package com.example.model

import android.content.Context
import android.net.Uri
import com.example.model.DependencyMetadata
import java.io.BufferedReader
import java.io.InputStreamReader
import java.util.zip.ZipInputStream

class DependencyResolver(private val context: Context) {

    /**
     * Programmatically scans an Android Project ZIP to extract build metadata.
     * Uses streaming to avoid high memory overhead for large ZIP files.
     */
    fun resolveDependencies(uri: Uri): DependencyMetadata {
        val libraries = mutableSetOf<String>()
        val plugins = mutableSetOf<String>()
        var hasAppModule = false
        var hasSrcModule = false

        context.contentResolver.openInputStream(uri)?.use { inputStream ->
            ZipInputStream(inputStream).use { zip ->
                var entry = zip.nextEntry
                while (entry != null) {
                    val name = entry.name
                    
                    // Critical structural validation
                    if (name.contains("app/src/main/")) hasAppModule = true
                    if (name.contains("/src/")) hasSrcModule = true

                    // Targeted Gradle parsing
                    if (name.endsWith("build.gradle") || name.endsWith("build.gradle.kts") || name.endsWith("libs.versions.toml")) {
                        val reader = BufferedReader(InputStreamReader(zip))
                        var line = reader.readLine()
                        while (line != null) {
                            extractLibraries(line, libraries)
                            extractPlugins(line, plugins)
                            line = reader.readLine()
                        }
                    }
                    zip.closeEntry()
                    entry = zip.nextEntry
                }
            }
        } ?: throw Exception("Source file is inaccessible")

        return DependencyMetadata(
            libraries = libraries.toList().sorted(),
            plugins = plugins.toList().sorted(),
            hasAppModule = hasAppModule,
            hasSrcModule = hasSrcModule
        )
    }

    private fun extractLibraries(line: String, libraries: MutableSet<String>) {
        val trimmed = line.trim()
        val regexPatterns = listOf(
            """implementation\s*\(?["']([^"']+)["']\)?""",
            """api\s*\(?["']([^"']+)["']\)?""",
            """alias\s*\(libs\.([^)]+)\)"""
        )

        regexPatterns.forEach { pattern ->
            Regex(pattern).find(trimmed)?.groupValues?.get(1)?.let { libraries.add(it) }
        }
    }

    private fun extractPlugins(line: String, plugins: MutableSet<String>) {
        val trimmed = line.trim()
        val patterns = listOf(
            """id\s*\(?["']([^"']+)["']\)?""",
            """apply\s*plugin:\s*["']([^"']+)["']"""
        )

        patterns.forEach { pattern ->
            Regex(pattern).find(trimmed)?.groupValues?.get(1)?.let { plugins.add(it) }
        }
        
        if (trimmed.startsWith("kotlin(")) {
             Regex("""["']([^"']+)["']""").find(trimmed)?.groupValues?.get(1)?.let { 
                 plugins.add("org.jetbrains.kotlin.$it") 
             }
        }
    }
}

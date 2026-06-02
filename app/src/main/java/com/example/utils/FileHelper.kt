package com.example.utils

import android.content.Context
import android.net.Uri
import java.io.File
import java.io.FileOutputStream
import java.util.zip.ZipInputStream

object FileHelper {
    fun extractProject(context: Context, uri: Uri): String {
        val extractDir = File(context.getExternalFilesDir(null), "download/extract/${System.currentTimeMillis()}")
        if (!extractDir.exists()) extractDir.mkdirs()

        val inputStream = context.contentResolver.openInputStream(uri) ?: return ""
        val zipInputStream = ZipInputStream(inputStream)
        
        var entry = zipInputStream.nextEntry
        while (entry != null) {
            val file = File(extractDir, entry.name)
            if (entry.isDirectory) {
                file.mkdirs()
            } else {
                file.parentFile?.mkdirs()
                FileOutputStream(file).use { output ->
                    zipInputStream.copyTo(output)
                }
            }
            zipInputStream.closeEntry()
            entry = zipInputStream.nextEntry
        }
        zipInputStream.close()
        return extractDir.absolutePath
    }
}

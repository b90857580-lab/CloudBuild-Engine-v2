package com.example.model

import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class BuildJob(
    val id: String,
    val projectName: String,
    val status: BuildStatus,
    val createdAt: Long,
    val logs: List<String> = emptyList(),
    val artifactUrl: String? = null
)

@JsonClass(generateAdapter = true)
data class DependencyMetadata(
    val libraries: List<String>,
    val plugins: List<String>,
    val hasAppModule: Boolean,
    val hasSrcModule: Boolean
)

enum class BuildStatus {
    QUEUED, COMPILING, SUCCESS, FAILED
}

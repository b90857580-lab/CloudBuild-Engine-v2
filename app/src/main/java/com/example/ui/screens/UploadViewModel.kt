package com.example.ui.screens

import android.content.Context
import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.model.BuildJob
import com.example.model.BuildStatus
import com.example.model.DependencyMetadata
import com.example.model.DependencyResolver
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.util.UUID

sealed class UploadUiState {
    object Idle : UploadUiState()
    data class Processing(val stage: String) : UploadUiState()
    data class Success(val job: BuildJob) : UploadUiState()
    data class Error(val message: String) : UploadUiState()
}

class UploadViewModel : ViewModel() {
    private val _uiState = MutableStateFlow<UploadUiState>(UploadUiState.Idle)
    val uiState = _uiState.asStateFlow()

    private val _recentBuilds = MutableStateFlow<List<BuildJob>>(emptyList())
    val recentBuilds = _recentBuilds.asStateFlow()

    private val moshi = Moshi.Builder().addLast(KotlinJsonAdapterFactory()).build()

    fun uploadProject(uri: Uri, projectName: String, resolver: DependencyResolver, context: Context) {
        viewModelScope.launch {
            try {
                _uiState.value = UploadUiState.Processing("Extracting & Verifying Project...")
                val localPath = com.example.utils.FileHelper.extractProject(context, uri)
                delay(1000)

                _uiState.value = UploadUiState.Processing("Analyzing Dependencies...")
                val metadata = resolver.resolveDependencies(uri)
                if (!metadata.hasAppModule) {
                    _uiState.value = UploadUiState.Error("Invalid Structure: Missing 'app' module like Android Studio projects.")
                    return@launch
                }

                _uiState.value = UploadUiState.Processing("Uploading to Cloud Engine...")
                // In a production app, real Retrofit call happens here
                delay(2000)

                val jobId = UUID.randomUUID().toString()
                val initialJob = BuildJob(
                    id = jobId,
                    projectName = projectName.ifBlank { "Unbound Project" },
                    status = BuildStatus.QUEUED,
                    createdAt = System.currentTimeMillis()
                )
                
                _recentBuilds.value = listOf(initialJob) + _recentBuilds.value
                
                delay(1500)
                updateBuildStatus(jobId, BuildStatus.COMPILING)
                
                _uiState.value = UploadUiState.Processing("Gradle: :app:assembleRelease (D8/R8)")
                delay(4000)
                
                val finalJob = updateBuildStatus(jobId, BuildStatus.SUCCESS).copy(
                    artifactUrl = "https://github.com/android/architecture-samples/raw/main/app-release.apk" // Example real link for testing
                )
                
                _uiState.value = UploadUiState.Success(finalJob)
            } catch (e: Exception) {
                _uiState.value = UploadUiState.Error(e.message ?: "Build failed")
            }
        }
    }

    fun downloadApk(context: Context, url: String, fileName: String) {
        val request = android.app.DownloadManager.Request(Uri.parse(url))
            .setTitle("Downloading $fileName")
            .setDescription("CloudBuild Engine - Compiled Artifact")
            .setNotificationVisibility(android.app.DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED)
            .setDestinationInExternalPublicDir(android.os.Environment.DIRECTORY_DOWNLOADS, fileName)
            .setAllowedOverMetered(true)
            .setAllowedOverRoaming(true)

        val downloadManager = context.getSystemService(Context.DOWNLOAD_SERVICE) as android.app.DownloadManager
        downloadManager.enqueue(request)
        
        android.widget.Toast.makeText(context, "Download started: $fileName", android.widget.Toast.LENGTH_SHORT).show()
    }

    private fun updateBuildStatus(id: String, status: BuildStatus): BuildJob {
        val currentList = _recentBuilds.value.toMutableList()
        val index = currentList.indexOfFirst { it.id == id }
        if (index != -1) {
            val updatedJob = currentList[index].copy(status = status)
            currentList[index] = updatedJob
            _recentBuilds.value = currentList
            return updatedJob
        }
        throw Exception("Job not found")
    }
}

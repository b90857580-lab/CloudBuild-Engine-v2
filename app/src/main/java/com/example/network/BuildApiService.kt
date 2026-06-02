package com.example.network

import com.example.model.BuildJob
import okhttp3.MultipartBody
import okhttp3.RequestBody
import retrofit2.http.*

interface BuildApiService {
    @Multipart
    @POST("builds/upload")
    suspend fun uploadProject(
        @Part project: MultipartBody.Part,
        @Part("projectName") name: RequestBody,
        @Part("metadata") metadata: RequestBody
    ): BuildJob

    @GET("builds/{id}")
    suspend fun getBuildStatus(@Path("id") id: String): BuildJob

    @GET("builds")
    suspend fun getAllBuilds(): List<BuildJob>

    @Streaming
    @GET("builds/{id}/download")
    suspend fun downloadArtifact(@Path("id") id: String): okhttp3.ResponseBody
}

# CloudBuild Engine - Technical Documentation

CloudBuild Engine is a specialized Android application designed for remote, cloud-based compilation of Android Studio projects.

## Architecture Highlights
- **Architecture**: MVVM with Clean Architecture principles.
- **UI Framework**: Jetpack Compose (Material 3) with Edge-to-Edge support.
- **Networking**: Retrofit 2 + OkHttp 4 for high-performance multipart uploads and streaming downloads.
- **Concurrency**: Kotlin Coroutines & StateFlow for non-blocking UI and background processing.
- **Dependency Analytics**: Custom implementation of `ZipInputStream` based Gradle parsing for pre-build optimization.

## Required Dependencies (build.gradle.kts)
```kotlin
dependencies {
    // UI & Navigation
    implementation(libs.androidx.compose.material3)
    implementation(libs.androidx.navigation.compose)
    implementation(libs.androidx.lifecycle.runtime.compose)
    
    // Networking
    implementation("com.squareup.retrofit2:retrofit:2.9.0")
    implementation("com.squareup.retrofit2:converter-moshi:2.9.0")
    implementation("com.squareup.okhttp3:okhttp:4.10.0")
    implementation("com.squareup.okhttp3:logging-interceptor:4.10.0")
    
    // JSON Serialization
    implementation("com.squareup.moshi:moshi-kotlin:1.14.0")
    
    // Persistence
    implementation(libs.androidx.room.ktx)
    implementation(libs.androidx.room.runtime)
    ksp(libs.androidx.room.compiler)
}
```

## Backend Integration & Server Linking

To bridge the Android client with a remote build environment:

1. **API Endpoint**: Configure the `BASE_URL` in your Retrofit client to point to a server running a containerized Android SDK environment.
2. **Build Execution**: 
   - The server receives the ZIP and `DependencyMetadata`.
   - It executes `./gradlew assembleRelease` which triggers the **D8/R8** tasks (shrinking, obfuscation, and optimization).
   - Sign the generated APK using `apksigner` with your cloud-stored keystore.
3. **Polling/Streaming**: Use the `Streaming` endpoint in `BuildApiService` to pipe the compiled binary back to the mobile device.

## Advanced Build Tasks
The engine is configured to trigger:
- `minifyEnabled true` (R8 Obfuscation)
- `shrinkResources true` (Unused resource removal)
- `multiDexEnabled true`


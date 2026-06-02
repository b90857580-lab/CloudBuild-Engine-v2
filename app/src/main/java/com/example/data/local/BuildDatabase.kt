package com.example.data.local

import androidx.room.*
import com.example.model.BuildJob
import com.example.model.BuildStatus
import kotlinx.coroutines.flow.Flow

@Entity(tableName = "build_jobs")
data class BuildJobEntity(
    @PrimaryKey val id: String,
    val projectName: String,
    val status: String,
    val createdAt: Long,
    val artifactUrl: String?
)

@Dao
interface BuildJobDao {
    @Query("SELECT * FROM build_jobs ORDER BY createdAt DESC")
    fun getAllBuilds(): Flow<List<BuildJobEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertBuild(job: BuildJobEntity)

    @Update
    suspend fun updateBuild(job: BuildJobEntity)
}

@Database(entities = [BuildJobEntity::class], version = 1)
abstract class BuildDatabase : RoomDatabase() {
    abstract fun buildJobDao(): BuildJobDao
}

package com.originalityshield.data

import android.content.Context
import androidx.room.*

@Entity(tableName = "offline_tasks")
data class OfflineTask(
    @PrimaryKey val taskId: Int,
    val artworkTitle: String,
    val imageUrl: String,
    val x: Int,
    val y: Int,
    val width: Int,
    val height: Int,
    val status: String,
    // Store drawing locally before sync
    val localDrawingPath: String? = null
)

@Dao
interface TaskDao {
    @Query("SELECT * FROM offline_tasks")
    suspend fun getAll(): List<OfflineTask>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(tasks: List<OfflineTask>)

    @Update
    suspend fun update(task: OfflineTask)
}

@Database(entities = [OfflineTask::class], version = 1)
abstract class TaskDatabase : RoomDatabase() {
    abstract fun taskDao(): TaskDao

    companion object {
        @Volatile
        private var INSTANCE: TaskDatabase? = null

        fun getDatabase(context: Context): TaskDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    TaskDatabase::class.java,
                    "ddcp_database"
                ).build()
                INSTANCE = instance
                instance
            }
        }
    }
}

package com.example.data.database

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface JobLogDao {
    @Query("SELECT * FROM job_logs ORDER BY timestamp DESC")
    fun getAllLogs(): Flow<List<JobLog>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertLog(log: JobLog)

    @Query("DELETE FROM job_logs WHERE id = :id")
    suspend fun deleteLogById(id: Int)

    @Query("DELETE FROM job_logs")
    suspend fun clearAllLogs()
}

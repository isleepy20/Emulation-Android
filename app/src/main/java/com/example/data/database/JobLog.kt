package com.example.data.database

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "job_logs")
data class JobLog(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val jobName: String,
    val status: String, // "SUCCESS", "RUNNING", "FAILED"
    val timestamp: Long = System.currentTimeMillis(),
    val payloadSize: String,
    val executionTimeMs: Long,
    val message: String
)

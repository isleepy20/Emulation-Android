package com.example.data.repository

import com.example.data.database.JobLog
import com.example.data.database.JobLogDao
import kotlinx.coroutines.flow.Flow

class JobRepository(private val jobLogDao: JobLogDao) {
    val allLogs: Flow<List<JobLog>> = jobLogDao.getAllLogs()

    suspend fun insertLog(log: JobLog) {
        jobLogDao.insertLog(log)
    }

    suspend fun deleteLogById(id: Int) {
        jobLogDao.deleteLogById(id)
    }

    suspend fun clearAllLogs() {
        jobLogDao.clearAllLogs()
    }
}

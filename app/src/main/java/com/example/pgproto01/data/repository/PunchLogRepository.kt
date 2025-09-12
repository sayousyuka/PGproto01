package com.example.pgproto01.data.repository

import com.example.pgproto01.data.model.PunchLogDao
import com.example.pgproto01.data.model.PunchLog
import kotlinx.coroutines.flow.Flow

class PunchLogRepository(private val dao: PunchLogDao) {

    suspend fun insert(log: PunchLog) {
        dao.insert(log)
    }

    fun getPunchLogsForStaff(staffId: Long): Flow<List<PunchLog>> {
        return dao.getByStaffId(staffId)
    }

    suspend fun clearAll() {
        dao.clearAll()
    }
}

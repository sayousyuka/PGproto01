package com.example.pgproto01.data.model

import androidx.room.*
import kotlinx.coroutines.flow.Flow


@Dao
interface PunchLogDao {
    @Insert
    suspend fun insert(log: PunchLog)

    @Delete
    suspend fun delete(log: PunchLog)  // ✅ これを追加！

    @Query("SELECT * FROM punch_logs WHERE staffId = :staffId ORDER BY date, time")
    fun getByStaffId(staffId: Long): Flow<List<PunchLog>>

    @Query("DELETE FROM punch_logs")
    suspend fun clearAll()
}


//@Dao
//interface PunchLogDao {
//
//    @Insert(onConflict = OnConflictStrategy.REPLACE)
//    suspend fun insert(log: PunchLog): Long
//
//    @Query("SELECT * FROM punch_logs WHERE staffId = :staffId ORDER BY date, time")
//    suspend fun getLogsForStaff(staffId: Long): List<PunchLog>
//
//    @Query("SELECT * FROM punch_logs WHERE date = :date ORDER BY time")
//    suspend fun getLogsByDate(date: String): List<PunchLog>
//
//    @Delete
//    suspend fun deleteLog(log: PunchLog)
//
//    @Query("UPDATE punch_logs SET isDeleted = 1 WHERE id = :id")
//    suspend fun markAsDeleted(id: Long)
//
//    @Query("SELECT * FROM punch_logs ORDER BY date DESC, time DESC")
//    suspend fun getAllLogs(): List<PunchLog>
//
//    @Delete
//    suspend fun delete(log: PunchLog) // ← これを追加
//
//    @Query("DELETE FROM punch_logs")
//    suspend fun clearAll()            // ← これも追加
//}

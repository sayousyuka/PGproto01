package com.example.pgproto01.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import com.example.pgproto01.data.model.PunchLog
import com.example.pgproto01.data.dao.PunchLogDao

@Database(
    entities = [PunchLog::class],
    version = 1,
    exportSchema = false
)
abstract class AttendanceDatabase : RoomDatabase() {
    abstract fun punchLogDao(): PunchLogDao
}

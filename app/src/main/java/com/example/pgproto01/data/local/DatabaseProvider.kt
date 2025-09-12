package com.example.pgproto01.data.local

import android.content.Context
import androidx.room.Room

object DatabaseProvider {
    private var INSTANCE: AttendanceDatabase? = null

    fun getDatabase(context: Context): AttendanceDatabase {
        return INSTANCE ?: synchronized(this) {
            Room.databaseBuilder(
                context.applicationContext,
                AttendanceDatabase::class.java,
                "attendance.db"
            ).build().also { INSTANCE = it }
        }
    }
}

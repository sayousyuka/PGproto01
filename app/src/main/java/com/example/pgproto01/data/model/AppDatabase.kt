package com.example.pgproto01.data.model

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(entities = [PunchLog::class], version = 1)
abstract class AppDatabase : RoomDatabase() {
    abstract fun punchLogDao(): PunchLogDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "punch_log_database"
                ).build()
                INSTANCE = instance
                instance
            }
        }
    }
}

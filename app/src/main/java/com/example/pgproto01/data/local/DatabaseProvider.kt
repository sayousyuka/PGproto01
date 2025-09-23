package com.example.pgproto01.data.local

import android.content.Context
import androidx.room.Room
import com.example.pgproto01.data.model.AppDatabase

object DatabaseProvider {
    @Volatile
    private var INSTANCE: AppDatabase? = null

    fun getDatabase(context: Context): AppDatabase {
        return INSTANCE ?: synchronized(this) {
            Room.databaseBuilder(
                context.applicationContext,
                AppDatabase::class.java,
                "punch_log_database" // ← AppDatabase と同じ名前にする
            )
                .fallbackToDestructiveMigration() // 開発中はこれでOK
                .build()
                .also { INSTANCE = it }
        }
    }
}

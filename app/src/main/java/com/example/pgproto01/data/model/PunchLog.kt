package com.example.pgproto01.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "punch_logs")
data class PunchLog(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val staffId: Long,
    val date: String,
    val time: String,
    val type: String,
    val isManual: Boolean = false,
    val isDeleted: Boolean = false,
    val createdAt: Long = System.currentTimeMillis()
)

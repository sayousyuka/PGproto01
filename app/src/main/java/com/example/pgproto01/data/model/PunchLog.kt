package com.example.pgproto01.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "punch_logs")
data class PunchLog(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val staffId: String,      // ← String に修正済み
    val date: String,         // ← 別行に分ける
    val time: String,         // "09:30"
    val type: String,         // "IN", "OUT" など
    val isManual: Boolean = false,
    val isDeleted: Boolean = false,
    val comment: String? = null,
    val createdAt: Long = System.currentTimeMillis()
)


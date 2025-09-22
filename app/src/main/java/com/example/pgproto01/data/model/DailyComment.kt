package com.example.pgproto01.data.model

import androidx.room.Entity

@Entity(
    tableName = "daily_comments",
    primaryKeys = ["staffId", "date"]
)
data class DailyComment(
    val staffId: String,    // スタッフID
    val date: String,       // yyyy-MM-dd 形式
    val comment: String     // その日の備考
)

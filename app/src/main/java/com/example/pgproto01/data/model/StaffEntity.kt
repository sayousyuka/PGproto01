package com.example.pgproto01.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "staffs")
data class StaffEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String,
    val kana: String? = null,    // 検索用のフリガナ（オプション）
    val isActive: Boolean = true // 退職・休職時に false にできる
)

package com.example.pgproto01.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "staffs")
data class StaffEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String,
    val kana: String = "",
    val isActive: Boolean = true
)

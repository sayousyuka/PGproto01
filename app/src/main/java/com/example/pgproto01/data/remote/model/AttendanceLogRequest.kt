// AttendanceLogRequest.kt

package com.example.pgproto01.data.remote.model

import kotlinx.serialization.Serializable

@Serializable
data class AttendanceLogRequest(
    val staff_id: String,
    val datetime: String,
    val type: String,
    val is_manual: Boolean,
    val remarks: String
)

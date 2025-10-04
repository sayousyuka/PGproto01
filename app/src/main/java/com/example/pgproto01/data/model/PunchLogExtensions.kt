// ファイル例: PunchLogExtensions.kt（場所はどこでもOK。例：data.model内）

package com.example.pgproto01.data.model

import com.example.pgproto01.data.remote.model.AttendanceLogRequest
import java.time.Instant
import java.time.LocalDateTime
import java.time.ZoneId

fun PunchLog.toRequest(): AttendanceLogRequest {
    val dateTime = Instant.ofEpochMilli(this.timestamp)
        .atZone(ZoneId.systemDefault())
        .toLocalDateTime()

    return AttendanceLogRequest(
        staff_id = this.staffId.toString(),     // LambdaはStringを期待
        datetime = dateTime.toString(),         // 例: "2025-10-02T09:00:00"
        type = this.type,
        is_manual = this.isManual,
        remarks = this.comment ?: ""
    )
}

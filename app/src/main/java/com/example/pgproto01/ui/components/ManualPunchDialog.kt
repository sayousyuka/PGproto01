package com.example.pgproto01.ui.component

import android.app.TimePickerDialog
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.format.DateTimeFormatter
import com.example.pgproto01.data.model.PunchType  // ✅ 修正済み
import com.example.pgproto01.viewmodel.PunchLogViewModel
import com.example.pgproto01.data.model.PunchLog

@Composable
fun ManualPunchDialog(
    viewModel: PunchLogViewModel,
    staffId: String,
    onDismiss: () -> Unit
) {
    val missingPunches = viewModel.missingPunches
    val pendingType = viewModel.pendingType

    val today = LocalDate.now()
    val now = LocalTime.now()
    val timeMap = remember { mutableStateMapOf<PunchType, LocalTime>() }

    // UI本体
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("順序違反の補完打刻") },
        text = {
            Column {
                Text("不足している打刻があります。手動で入力してください。")
                Spacer(Modifier.height(8.dp))

                for (type in missingPunches) {
                    val label = when (type) {
                        PunchType.IN -> "出勤時刻"
                        PunchType.BREAK_OUT -> "外出時刻"
                        PunchType.BREAK_IN -> "戻り時刻"
                        PunchType.OUT -> "退勤時刻"
                    }

                    TimePickerField(
                        label = label,
                        initialTime = timeMap[type] ?: now,
                        onTimeSelected = { timeMap[type] = it }
                    )
                    Spacer(Modifier.height(4.dp))
                }
            }
        },
        confirmButton = {
            TextButton(onClick = {
                // ① 補完打刻（手動）
                for (type in missingPunches) {
                    val selectedTime = timeMap[type] ?: now
                    viewModel.insertPunchLog(
                        staffId = staffId.toLong(),
                        type = type,
                        timestamp = LocalDateTime.of(today, selectedTime),
                        isManual = true
                    )
                }

                // ② pendingTypeを現在時刻で自動打刻
                if (pendingType != null) {
                    viewModel.insertPunchLog(
                        staffId = staffId.toLong(),
                        type = pendingType,
                        timestamp = LocalDateTime.of(today, now),
                        isManual = false
                    )
                }

                // 状態リセット
                viewModel.updatePendingType(null)
                viewModel.updateMissingPunches(emptyList())
                viewModel.updateShowManualDialog(false)
                onDismiss()
            }) {
                Text("確定")
            }
        }
        ,
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("キャンセル")
            }
        }
    )
}
@Composable
fun TimePickerField(
    label: String,
    initialTime: LocalTime,
    onTimeSelected: (LocalTime) -> Unit
) {
    var timeText by remember { mutableStateOf(initialTime.format(DateTimeFormatter.ofPattern("HH:mm"))) }

    OutlinedTextField(
        value = timeText,
        onValueChange = {
            timeText = it
            runCatching {
                val parsed = LocalTime.parse(it, DateTimeFormatter.ofPattern("HH:mm"))
                onTimeSelected(parsed)
            }
        },
        label = { Text(label) },
        modifier = Modifier.fillMaxWidth()
    )
}


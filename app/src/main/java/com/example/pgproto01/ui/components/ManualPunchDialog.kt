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

@Composable
fun ManualPunchDialog(
    date: LocalDate,
    punchType: PunchType, // あなたの定義に応じて
    onDismiss: () -> Unit,
    onSave: (LocalDateTime, String) -> Unit
) {
    var selectedTime by remember { mutableStateOf(LocalTime.now()) }
    var comment by remember { mutableStateOf("") }
    var showTimePicker by remember { mutableStateOf(false) }

    // ↓ TimePickerDialog を表示
    if (showTimePicker) {
        val context = LocalContext.current
        TimePickerDialog(
            context,
            { _, hour: Int, minute: Int ->
                selectedTime = LocalTime.of(hour, minute)
                showTimePicker = false
            },
            selectedTime.hour,
            selectedTime.minute,
            true // 24時間表記
        ).show()
    }

    // ↓ AlertDialog 本体
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("手動打刻の追加") },
        text = {
            Column {
                Text("日付: ${date.format(DateTimeFormatter.ofPattern("MM/dd"))}")
                Text("区分: ${punchType.displayName()}")
                Spacer(Modifier.height(8.dp))

                // ✅ 選択式ボタンで時刻入力
                OutlinedButton(
                    onClick = { showTimePicker = true },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("時刻を選択: ${selectedTime.format(DateTimeFormatter.ofPattern("HH:mm"))}")
                }

                Spacer(Modifier.height(8.dp))

                OutlinedTextField(
                    value = comment,
                    onValueChange = { comment = it },
                    label = { Text("コメント") },
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            TextButton(onClick = {
                val dateTime = LocalDateTime.of(date, selectedTime)
                onSave(dateTime, comment)
            }) {
                Text("保存")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("キャンセル")
            }
        }
    )
}

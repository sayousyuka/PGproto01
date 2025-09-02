package com.example.pgproto01.ui.component

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.format.DateTimeFormatter
import com.example.pgproto01.data.model.PunchType  // ✅ 修正済み

@Composable
fun ManualPunchDialog(
    date: LocalDate,
    punchType: PunchType,
    onDismiss: () -> Unit,
    onSave: (LocalDateTime, String) -> Unit
) {
    var selectedTime by remember { mutableStateOf(LocalTime.now()) }
    var comment by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("手動打刻の追加") },
        text = {
            Column {
                Text("日付: ${date.format(DateTimeFormatter.ofPattern("MM/dd"))}")
                Text("区分: ${punchType.displayName()}") // ✅ displayName が enum に必要
                Spacer(Modifier.height(8.dp))

                // ✅ TimePicker の代用
                OutlinedTextField(
                    value = selectedTime.format(DateTimeFormatter.ofPattern("HH:mm")),
                    onValueChange = {
                        selectedTime = try {
                            LocalTime.parse(it, DateTimeFormatter.ofPattern("HH:mm"))
                        } catch (e: Exception) {
                            selectedTime
                        }
                    },
                    label = { Text("時刻 (HH:mm)") },
                    modifier = Modifier.fillMaxWidth()
                )

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

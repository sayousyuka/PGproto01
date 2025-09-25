package com.example.pgproto01.ui

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Checkbox
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.Alignment
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.foundation.text.KeyboardOptions
import com.example.pgproto01.data.model.StaffEntity

@Composable
fun EditStaffDialog(
    staff: StaffEntity,
    onDismiss: () -> Unit,
    onSave: (StaffEntity) -> Unit
) {
    var name by remember { mutableStateOf(staff.name) }
    var kana by remember { mutableStateOf(staff.kana ?: "") }
    var isActive by remember { mutableStateOf(staff.isActive) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("スタッフ編集") },
        text = {
            Column {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("名前") },
                    modifier = Modifier.fillMaxWidth(),
                    keyboardOptions = KeyboardOptions.Default.copy(imeAction = ImeAction.Next)
                )
                OutlinedTextField(
                    value = kana,
                    onValueChange = { kana = it },
                    label = { Text("かな") },
                    modifier = Modifier.fillMaxWidth(),
                    keyboardOptions = KeyboardOptions.Default.copy(imeAction = ImeAction.Done)
                )
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.padding(top = 8.dp)
                ) {
                    Checkbox(
                        checked = isActive,
                        onCheckedChange = { isActive = it }
                    )
                    Text("有効", modifier = Modifier.padding(start = 4.dp))
                }
            }
        },
        confirmButton = {
            TextButton(onClick = {
                onSave(
                    staff.copy(
                        name = name,
                        kana = kana,
                        isActive = isActive
                    )
                )
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

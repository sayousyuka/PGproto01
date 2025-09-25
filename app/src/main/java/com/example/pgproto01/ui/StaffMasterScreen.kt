package com.example.pgproto01.ui

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.graphics.Color
import androidx.navigation.NavController
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.ui.Alignment
import com.example.pgproto01.data.model.StaffEntity
import com.example.pgproto01.viewmodel.StaffViewModel
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack



@Composable
fun StaffMasterScreen(
    navController: NavController,
    viewModel: StaffViewModel = viewModel()
) {
    val staffList by viewModel.staffList.collectAsState() // ✅ 名前を合わせる
    var showDialog by remember { mutableStateOf(false) }
    var editingStaff by remember { mutableStateOf<StaffEntity?>(null) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("職員マスタ") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.Filled.ArrowBack, contentDescription = "戻る")
                    }
                }
            )
        }
    ) { padding ->
        Column(modifier = Modifier.padding(padding)) {
            LazyColumn {
                items(staffList) { staff: StaffEntity ->   // ★ 型を明示
                    ListItem(
                        headlineContent = {
                            Text(
                                staff.name,
                                fontWeight = if (staff.isActive) FontWeight.Normal else FontWeight.Light,
                                color = if (staff.isActive) LocalContentColor.current else Color.Gray
                            )
                        },
                        supportingContent = {
                            Text(
                                staff.kana ?: "",
                                color = if (staff.isActive) LocalContentColor.current else Color.Gray
                            )
                        },
                        modifier = Modifier.clickable {
                            editingStaff = staff
                            showDialog = true
                        }
                    )
                    HorizontalDivider() // ★ Divider() → HorizontalDivider()
                }
            }

        }
    }

    if (showDialog && editingStaff != null) {
        EditStaffDialog(
            staff = editingStaff!!,
            onDismiss = { showDialog = false },
            onSave = { updated ->
                viewModel.updateStaff(updated) // ✅ 正しい関数名
                showDialog = false
            }
        )
    }
}


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

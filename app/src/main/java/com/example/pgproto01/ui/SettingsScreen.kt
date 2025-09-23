@file:OptIn(ExperimentalMaterial3Api::class)

package com.example.attendance.ui


import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavController

@Composable
fun SettingsScreen(navController: NavController) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("設定") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.Filled.ArrowBack, contentDescription = "戻る")
                    }
                }
            )
        }
    ) { padding ->
        Column(modifier = Modifier.padding(padding)) {
            ListItem(
                headlineContent = { Text("職員マスタ") },
                modifier = Modifier.clickable {
                    navController.navigate("staffMaster")
                }
            )
            Divider()
            ListItem(
                headlineContent = { Text("通信設定（準備中）") }
            )
            Divider()
            ListItem(
                headlineContent = { Text("その他（準備中）") }
            )
        }
    }
}

package com.example.haojizhang.ui

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun BillsScreen() {
    Column(Modifier.fillMaxSize().padding(16.dp)) {
        Text("账单列表", style = MaterialTheme.typography.titleLarge)
        Spacer(Modifier.height(12.dp))
        Text("这里后面接 Room 数据库，展示每天的账单分组列表。")
    }
}

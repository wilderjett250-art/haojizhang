package com.example.haojizhang.ui

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun HomeScreen() {
    Column(Modifier.fillMaxSize().padding(16.dp)) {
        Text("本月概览", style = MaterialTheme.typography.titleLarge)
        Spacer(Modifier.height(12.dp))
        Card(Modifier.fillMaxWidth()) {
            Column(Modifier.padding(16.dp)) {
                Text("收入：¥ 0.00")
                Text("支出：¥ 0.00")
                Text("结余：¥ 0.00")
            }
        }
        Spacer(Modifier.height(12.dp))
        Text("快捷记账（示例）", style = MaterialTheme.typography.titleMedium)
        Spacer(Modifier.height(8.dp))
        AssistChip(onClick = {}, label = { Text("早餐 12") })
        Spacer(Modifier.height(8.dp))
        AssistChip(onClick = {}, label = { Text("地铁 3") })
    }
}

package com.example.haojizhang.ui

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun InsightsScreen() {
    Column(Modifier.fillMaxSize().padding(16.dp)) {
        Text("洞察", style = MaterialTheme.typography.titleLarge)
        Spacer(Modifier.height(12.dp))
        Text("这里后面做：分类占比图、支出趋势图、AI 月报。")
    }
}

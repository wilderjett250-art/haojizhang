package com.example.haojizhang.ui

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun HomeScreen() {
    Column(Modifier.fillMaxSize().padding(16.dp)) {
        Text("首页", style = MaterialTheme.typography.titleLarge)
        Spacer(Modifier.height(12.dp))
        Text("欢迎使用豪记账！")
        Spacer(Modifier.height(12.dp))
        Text("后续在此展示：当月收支汇总、快速记账入口等。")
    }
}

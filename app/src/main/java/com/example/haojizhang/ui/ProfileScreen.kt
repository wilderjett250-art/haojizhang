package com.example.haojizhang.ui

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun ProfileScreen() {
    Column(Modifier.fillMaxSize().padding(16.dp)) {
        Text("我的", style = MaterialTheme.typography.titleLarge)
        Spacer(Modifier.height(12.dp))
        Text("预算设置 / 分类管理 / 账户管理 / 数据导出（后面做）")
    }
}

package com.example.haojizhang.ui

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun ProfileScreen() {
    Column(Modifier.fillMaxSize().padding(16.dp)) {
        Text("个人中心", style = MaterialTheme.typography.titleLarge)
        Spacer(Modifier.height(12.dp))
        Text("姓名：孙亿豪")
        Text("学号：202305100226")
        Spacer(Modifier.height(12.dp))
        Text("这里可以添加应用设置、主题切换、数据备份等功能")
    }
}

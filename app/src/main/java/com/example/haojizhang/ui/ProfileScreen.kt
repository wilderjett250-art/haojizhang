package com.example.haojizhang.ui

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun ProfileScreen() {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Text("我的", style = MaterialTheme.typography.titleLarge)
        Spacer(Modifier.height(16.dp))

        Text("姓名：孙亿豪", style = MaterialTheme.typography.bodyLarge)
        Text("学号：202305100226", style = MaterialTheme.typography.bodyLarge)

        Spacer(Modifier.height(24.dp))

        Text(
            text = "项目说明：",
            style = MaterialTheme.typography.titleMedium
        )
        Spacer(Modifier.height(8.dp))
        Text(
            text = "「豪记账」是一款个人财务管理应用，支持日常记账、" +
                    "月度收支统计与支出分类分析，帮助用户清晰了解自己的消费情况。",
            style = MaterialTheme.typography.bodyMedium
        )
    }
}

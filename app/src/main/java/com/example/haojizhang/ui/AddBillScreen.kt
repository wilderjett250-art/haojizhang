package com.example.haojizhang.ui

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun AddBillScreen() {
    var amount by remember { mutableStateOf("") }
    var type by remember { mutableStateOf("支出") }

    Column(
        Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Text("新增账单", style = MaterialTheme.typography.titleLarge)
        Spacer(Modifier.height(16.dp))

        OutlinedTextField(
            value = amount,
            onValueChange = { amount = it },
            label = { Text("金额（元）") },
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(Modifier.height(12.dp))

        Text("类型：")
        Row {
            listOf("支出", "收入").forEach {
                FilterChip(
                    selected = type == it,
                    onClick = { type = it },
                    label = { Text(it) }
                )
                Spacer(Modifier.width(8.dp))
            }
        }

        Spacer(Modifier.height(24.dp))
        Button(
            onClick = { /* TODO: 保存逻辑后续加 */ },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("保存账单")
        }
    }
}

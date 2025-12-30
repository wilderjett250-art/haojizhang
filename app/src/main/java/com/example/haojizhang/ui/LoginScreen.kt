package com.example.haojizhang.ui

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.example.haojizhang.data.util.Prefs

@Composable
fun LoginScreen(onSuccess: () -> Unit) {
    val ctx = LocalContext.current
    val pinEnabled = remember { Prefs.isPinEnabled(ctx) }
    val savedPin = remember { Prefs.getPin(ctx) }

    // 如果没启用 PIN，直接放行（你也可以改成必须设置）
    LaunchedEffect(pinEnabled, savedPin) {
        if (!pinEnabled || savedPin.isBlank()) onSuccess()
    }

    var input by remember { mutableStateOf("") }
    var err by remember { mutableStateOf<String?>(null) }

    Column(Modifier.fillMaxSize().padding(16.dp)) {
        Text("登录", style = MaterialTheme.typography.titleLarge)
        Spacer(Modifier.height(12.dp))
        Text("请输入 PIN 进入豪记账", style = MaterialTheme.typography.bodyMedium)

        Spacer(Modifier.height(16.dp))
        OutlinedTextField(
            value = input,
            onValueChange = { input = it; err = null },
            label = { Text("PIN（4~8位数字）") },
            singleLine = true,
            modifier = Modifier.fillMaxWidth()
        )

        err?.let {
            Spacer(Modifier.height(10.dp))
            Text(it, color = MaterialTheme.colorScheme.error)
        }

        Spacer(Modifier.height(16.dp))
        Button(
            onClick = {
                if (input == savedPin) onSuccess() else err = "PIN 错误"
            },
            modifier = Modifier.fillMaxWidth()
        ) { Text("进入") }
    }
}

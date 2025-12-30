package com.example.haojizhang.ui

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.example.haojizhang.data.local.db.DbProvider
import com.example.haojizhang.data.local.entity.BudgetEntity
import com.example.haojizhang.data.util.DateUtils
import kotlinx.coroutines.launch
import java.math.BigDecimal
import java.math.RoundingMode

@Composable
fun ProfileScreen() {
    val context = LocalContext.current
    val db = remember { DbProvider.get(context) }

    val yearMonth = remember { DateUtils.currentYearMonth() }
    val budget by db.budgetDao().observeByMonth(yearMonth).collectAsState(initial = null)

    var showDialog by remember { mutableStateOf(false) }
    var inputBudget by remember { mutableStateOf("") }
    var errorMsg by remember { mutableStateOf<String?>(null) }

    val scope = rememberCoroutineScope()

    Column(Modifier.fillMaxSize().padding(16.dp)) {
        Text("个人中心", style = MaterialTheme.typography.titleLarge)
        Spacer(Modifier.height(12.dp))

        Card(Modifier.fillMaxWidth()) {
            Column(Modifier.padding(16.dp)) {
                Text("用户信息", style = MaterialTheme.typography.titleMedium)
                Spacer(Modifier.height(8.dp))
                Text("姓名：孙亿豪")
                Text("学号：202305100226")
            }
        }

        Spacer(Modifier.height(12.dp))

        Card(Modifier.fillMaxWidth()) {
            Column(Modifier.padding(16.dp)) {
                Text("预算设置（$yearMonth）", style = MaterialTheme.typography.titleMedium)
                Spacer(Modifier.height(8.dp))

                if (budget == null) {
                    Text("本月尚未设置预算。")
                } else {
                    Text("当前预算：¥${centToYuanText(budget!!.limitCent)}")
                }

                Spacer(Modifier.height(12.dp))
                Button(onClick = {
                    inputBudget = if (budget == null) "" else centToYuanText(budget!!.limitCent)
                    errorMsg = null
                    showDialog = true
                }) {
                    Text(if (budget == null) "设置预算" else "修改预算")
                }
            }
        }

        Spacer(Modifier.height(12.dp))
        Text("后续可在此添加：主题切换、数据备份/导出等。", style = MaterialTheme.typography.bodyMedium)
    }

    if (showDialog) {
        AlertDialog(
            onDismissRequest = { showDialog = false },
            title = { Text("设置本月预算（元）") },
            text = {
                Column {
                    OutlinedTextField(
                        value = inputBudget,
                        onValueChange = { inputBudget = it; errorMsg = null },
                        label = { Text("例如 1500 或 1500.00") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                    errorMsg?.let {
                        Spacer(Modifier.height(8.dp))
                        Text(it, color = MaterialTheme.colorScheme.error)
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = {
                    scope.launch {
                        val cent = parseYuanToCent(inputBudget)
                        if (cent == null || cent <= 0) {
                            errorMsg = "请输入正确的预算金额（必须大于 0）"
                            return@launch
                        }
                        val now = DateUtils.now()
                        val entity = BudgetEntity(
                            id = budget?.id ?: 0L,
                            yearMonth = yearMonth,
                            limitCent = cent,
                            createdAt = budget?.createdAt ?: now,
                            updatedAt = now
                        )
                        db.budgetDao().upsert(entity)
                        showDialog = false
                    }
                }) { Text("保存") }
            },
            dismissButton = {
                TextButton(onClick = { showDialog = false }) { Text("取消") }
            }
        )
    }
}

/** 分 -> 元（字符串），保留 2 位小数 */
private fun centToYuanText(cent: Long): String {
    val abs = kotlin.math.abs(cent)
    val yuan = abs / 100
    val fen = abs % 100
    return "$yuan.${fen.toString().padStart(2, '0')}"
}

/** 元文本 -> 分（Long） */
private fun parseYuanToCent(text: String): Long? {
    val t = text.trim()
    if (t.isBlank()) return null
    return try {
        val bd = BigDecimal(t).setScale(2, RoundingMode.HALF_UP)
        bd.multiply(BigDecimal(100)).longValueExact()
    } catch (_: Exception) {
        null
    }
}

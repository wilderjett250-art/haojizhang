package com.example.haojizhang.ui

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.example.haojizhang.data.local.db.DbProvider
import com.example.haojizhang.data.local.entity.BudgetEntity
import com.example.haojizhang.data.util.BackupJson
import com.example.haojizhang.data.util.BackupPayload
import com.example.haojizhang.data.util.DateUtils
import com.example.haojizhang.data.util.Prefs
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.BufferedReader
import java.io.InputStreamReader
import java.math.BigDecimal
import java.math.RoundingMode

@Composable
fun ProfileScreen() {
    val ctx = LocalContext.current
    val db = remember { DbProvider.get(ctx) }
    val scope = rememberCoroutineScope()

    val yearMonth = remember { DateUtils.currentYearMonth() }
    val budget by db.budgetDao().observeByMonth(yearMonth).collectAsState(initial = null)

    val snackbarHostState = remember { SnackbarHostState() }
    fun snack(msg: String) { scope.launch { snackbarHostState.showSnackbar(msg) } }

    // ====== 预算 Dialog ======
    var showBudgetDialog by remember { mutableStateOf(false) }
    var inputBudget by remember { mutableStateOf("") }
    var budgetErr by remember { mutableStateOf<String?>(null) }

    // ====== 规则 Dialog ======
    var showRuleDialog by remember { mutableStateOf(false) }
    var inputHigh by remember { mutableStateOf("") }
    var inputLow by remember { mutableStateOf("") }
    var inputTop by remember { mutableStateOf("") }
    var ruleErr by remember { mutableStateOf<String?>(null) }

    // ====== PIN Dialog ======
    var showPinDialog by remember { mutableStateOf(false) }
    var inputPin by remember { mutableStateOf("") }
    var pinErr by remember { mutableStateOf<String?>(null) }

    // ====== 导出 ======
    val exportLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.CreateDocument("application/json")
    ) { uri: Uri? ->
        if (uri == null) return@rememberLauncherForActivityResult
        scope.launch {
            runCatching {
                val payload = withContext(Dispatchers.IO) {
                    BackupPayload(
                        categories = db.categoryDao().getAllForExport(),
                        accounts = db.accountDao().getAllForExport(),
                        budgets = db.budgetDao().getAllForExport(),
                        bills = db.billDao().getAllForExport()
                    )
                }
                val json = BackupJson.encode(payload)
                withContext(Dispatchers.IO) {
                    ctx.contentResolver.openOutputStream(uri, "wt")!!.use { os ->
                        os.write(json.toByteArray(Charsets.UTF_8))
                    }
                }
            }.onSuccess {
                snack("导出成功")
            }.onFailure {
                snack("导出失败：${it.message}")
            }
        }
    }

    // ====== 导入 ======
    val importLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.OpenDocument()
    ) { uri: Uri? ->
        if (uri == null) return@rememberLauncherForActivityResult
        scope.launch {
            runCatching {
                val text = withContext(Dispatchers.IO) {
                    ctx.contentResolver.openInputStream(uri)!!.use { ins ->
                        BufferedReader(InputStreamReader(ins, Charsets.UTF_8)).readText()
                    }
                }
                val payload = BackupJson.decode(text)

                withContext(Dispatchers.IO) {
                    db.runInTransaction {
                        // 用非 suspend 方式：事务内部不直接调用 suspend
                        // 所以我们这里不用 DAO 的 suspend deleteAll，而是把清空放到事务外更规范
                    }
                }

                // ✅ 更简单安全：事务外用协程清空 + 导入（仍然保证顺序）
                withContext(Dispatchers.IO) {
                    db.billDao().deleteAll()
                    db.categoryDao().deleteAll()
                    db.accountDao().deleteAll()
                    db.budgetDao().deleteAll()

                    db.categoryDao().upsertAll(payload.categories)
                    db.accountDao().upsertAll(payload.accounts)
                    payload.budgets.forEach { db.budgetDao().upsert(it) }
                    db.billDao().insertAllForImport(payload.bills)
                }
            }.onSuccess {
                snack("导入成功（已覆盖本地数据）")
            }.onFailure {
                snack("导入失败：${it.message}")
            }
        }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { inner ->
        Column(
            Modifier.fillMaxSize().padding(inner).padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text("个人中心", style = MaterialTheme.typography.titleLarge)

            Card(Modifier.fillMaxWidth()) {
                Column(Modifier.padding(16.dp)) {
                    Text("用户信息", style = MaterialTheme.typography.titleMedium)
                    Spacer(Modifier.height(8.dp))
                    Text("姓名：孙亿豪")
                    Text("学号：202305100226")
                }
            }

            Card(Modifier.fillMaxWidth()) {
                Column(Modifier.padding(16.dp)) {
                    Text("预算（$yearMonth）", style = MaterialTheme.typography.titleMedium)
                    Spacer(Modifier.height(8.dp))
                    Text(if (budget == null) "未设置" else "当前：¥${centToYuanText(budget!!.limitCent)}")
                    Spacer(Modifier.height(10.dp))
                    Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                        Button(onClick = {
                            inputBudget = budget?.let { centToYuanText(it.limitCent) } ?: ""
                            budgetErr = null
                            showBudgetDialog = true
                        }) { Text(if (budget == null) "设置预算" else "修改预算") }

                        OutlinedButton(onClick = {
                            inputHigh = ((Prefs.getWarnHigh(ctx) * 100).toInt()).toString()
                            inputLow = ((Prefs.getWarnLow(ctx) * 100).toInt()).toString()
                            inputTop = ((Prefs.getTopShare(ctx) * 100).toInt()).toString()
                            ruleErr = null
                            showRuleDialog = true
                        }) { Text("预算规则") }
                    }
                }
            }

            Card(Modifier.fillMaxWidth()) {
                Column(Modifier.padding(16.dp)) {
                    Text("数据备份", style = MaterialTheme.typography.titleMedium)
                    Spacer(Modifier.height(10.dp))
                    Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                        Button(onClick = { exportLauncher.launch("haojizhang_backup.json") }) { Text("导出") }
                        OutlinedButton(onClick = { importLauncher.launch(arrayOf("application/json", "text/*")) }) { Text("导入") }
                    }
                    Spacer(Modifier.height(6.dp))
                    Text("导入会覆盖本地数据（稳定策略）", style = MaterialTheme.typography.bodySmall)
                }
            }

            Card(Modifier.fillMaxWidth()) {
                Column(Modifier.padding(16.dp)) {
                    Text("登录锁（本地 PIN）", style = MaterialTheme.typography.titleMedium)
                    Spacer(Modifier.height(8.dp))
                    val enabled = Prefs.isPinEnabled(ctx) && Prefs.getPin(ctx).isNotBlank()
                    Text(if (enabled) "已启用" else "未启用")
                    Spacer(Modifier.height(10.dp))
                    Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                        Button(onClick = {
                            inputPin = ""
                            pinErr = null
                            showPinDialog = true
                        }) { Text(if (enabled) "修改PIN" else "设置PIN") }

                        if (enabled) {
                            OutlinedButton(onClick = {
                                Prefs.setPin(ctx, "")
                                snack("已关闭 PIN")
                            }) { Text("关闭") }
                        }
                    }
                }
            }
        }
    }

    // ===== 预算 Dialog =====
    if (showBudgetDialog) {
        AlertDialog(
            onDismissRequest = { showBudgetDialog = false },
            title = { Text("设置本月预算（元）") },
            text = {
                Column {
                    OutlinedTextField(
                        value = inputBudget,
                        onValueChange = { inputBudget = it; budgetErr = null },
                        label = { Text("例如 1500 或 1500.00") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                    budgetErr?.let { Spacer(Modifier.height(8.dp)); Text(it, color = MaterialTheme.colorScheme.error) }
                }
            },
            confirmButton = {
                TextButton(onClick = {
                    scope.launch {
                        val cent = parseYuanToCent(inputBudget)
                        if (cent == null || cent <= 0) {
                            budgetErr = "请输入正确金额（>0）"
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
                        showBudgetDialog = false
                        snack("预算已保存")
                    }
                }) { Text("保存") }
            },
            dismissButton = { TextButton(onClick = { showBudgetDialog = false }) { Text("取消") } }
        )
    }

    // ===== 规则 Dialog =====
    if (showRuleDialog) {
        AlertDialog(
            onDismissRequest = { showRuleDialog = false },
            title = { Text("预算规则设置（百分比）") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    OutlinedTextField(
                        value = inputHigh,
                        onValueChange = { inputHigh = it; ruleErr = null },
                        label = { Text("偏高预警阈值（如 85）") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = inputLow,
                        onValueChange = { inputLow = it; ruleErr = null },
                        label = { Text("偏低阈值（如 40）") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = inputTop,
                        onValueChange = { inputTop = it; ruleErr = null },
                        label = { Text("Top分类占比预警（如 60）") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                    ruleErr?.let { Text(it, color = MaterialTheme.colorScheme.error) }
                }
            },
            confirmButton = {
                TextButton(onClick = {
                    val high = inputHigh.toIntOrNull()
                    val low = inputLow.toIntOrNull()
                    val top = inputTop.toIntOrNull()
                    if (high == null || low == null || top == null) {
                        ruleErr = "请输入数字百分比"
                        return@TextButton
                    }
                    if (high !in 50..100 || low !in 0..80 || top !in 10..100) {
                        ruleErr = "范围建议：高(50~100) 低(0~80) Top(10~100)"
                        return@TextButton
                    }
                    Prefs.setRules(ctx, high / 100f, low / 100f, top / 100f)
                    showRuleDialog = false
                    snack("规则已保存（洞察按新阈值判断）")
                }) { Text("保存") }
            },
            dismissButton = { TextButton(onClick = { showRuleDialog = false }) { Text("取消") } }
        )
    }

    // ===== PIN Dialog =====
    if (showPinDialog) {
        AlertDialog(
            onDismissRequest = { showPinDialog = false },
            title = { Text("设置 PIN（4~8位数字）") },
            text = {
                Column {
                    OutlinedTextField(
                        value = inputPin,
                        onValueChange = { inputPin = it; pinErr = null },
                        label = { Text("PIN") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                    pinErr?.let { Spacer(Modifier.height(8.dp)); Text(it, color = MaterialTheme.colorScheme.error) }
                }
            },
            confirmButton = {
                TextButton(onClick = {
                    val p = inputPin.trim()
                    if (p.length !in 4..8 || p.any { !it.isDigit() }) {
                        pinErr = "PIN 必须是 4~8 位数字"
                        return@TextButton
                    }
                    Prefs.setPin(ctx, p)
                    showPinDialog = false
                    snack("PIN 已设置，下次启动需登录")
                }) { Text("保存") }
            },
            dismissButton = { TextButton(onClick = { showPinDialog = false }) { Text("取消") } }
        )
    }
}

private fun centToYuanText(cent: Long): String {
    val abs = kotlin.math.abs(cent)
    val yuan = abs / 100
    val fen = abs % 100
    return "$yuan.${fen.toString().padStart(2, '0')}"
}

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

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
import com.example.haojizhang.data.util.DateUtils
import com.example.haojizhang.data.util.Prefs
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.math.BigDecimal
import java.math.RoundingMode

@Composable
fun ProfileScreen() {
    val ctx = LocalContext.current
    val db = remember { DbProvider.get(ctx) }
    val scope = rememberCoroutineScope()

    val snackbarHostState = remember { SnackbarHostState() }
    fun snack(msg: String) { scope.launch { snackbarHostState.showSnackbar(msg) } }

    val yearMonth = remember { DateUtils.currentYearMonth() }
    val budget by db.budgetDao().observeByMonth(yearMonth).collectAsState(initial = null)

    // ====== 预算 Dialog ======
    var showBudgetDialog by remember { mutableStateOf(false) }
    var inputBudget by remember { mutableStateOf("") }
    var budgetErr by remember { mutableStateOf<String?>(null) }

    // ====== 规则 Dialog ======
    var showRuleDialog by remember { mutableStateOf(false) }
    var inputHigh by remember { mutableStateOf("") } // 85
    var inputLow by remember { mutableStateOf("") }  // 40
    var inputTop by remember { mutableStateOf("") }  // 60
    var ruleErr by remember { mutableStateOf<String?>(null) }

    // ====== PIN Dialog ======
    var showPinDialog by remember { mutableStateOf(false) }
    var inputPin by remember { mutableStateOf("") }
    var pinErr by remember { mutableStateOf<String?>(null) }

    // ====== 导出 CSV ======
    val exportCsvLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.CreateDocument("text/csv")
    ) { uri: Uri? ->
        if (uri == null) return@rememberLauncherForActivityResult
        scope.launch {
            runCatching {
                val rows = withContext(Dispatchers.IO) {
                    // 需要你在 BillDao 增加 getAllForCsv()
                    db.billDao().getAllForCsv()
                }
                val csv = buildCsv(rows)

                withContext(Dispatchers.IO) {
                    ctx.contentResolver.openOutputStream(uri, "wt")!!.use { os ->
                        // ✅ Excel 识别中文更稳：写入 UTF-8 BOM
                        os.write(byteArrayOf(0xEF.toByte(), 0xBB.toByte(), 0xBF.toByte()))
                        os.write(csv.toByteArray(Charsets.UTF_8))
                    }
                }
            }.onSuccess {
                snack("CSV 导出成功（电脑用 Excel 可直接打开）")
            }.onFailure {
                snack("CSV 导出失败：${it.message}")
            }
        }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { inner ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(inner)
                .padding(16.dp),
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

            // ===== 预算与规则 =====
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

            // ===== 数据导出：CSV =====
            Card(Modifier.fillMaxWidth()) {
                Column(Modifier.padding(16.dp)) {
                    Text("数据导出", style = MaterialTheme.typography.titleMedium)
                    Spacer(Modifier.height(10.dp))
                    OutlinedButton(
                        onClick = { exportCsvLauncher.launch("haojizhang_bills.csv") },
                        modifier = Modifier.fillMaxWidth()
                    ) { Text("导出账单 CSV（Excel）") }

                    Spacer(Modifier.height(6.dp))
                    Text("说明：CSV 可直接用 Excel 打开；包含分类名、账户名、备注、时间戳。",
                        style = MaterialTheme.typography.bodySmall)
                }
            }

            // ===== 登录锁（本地 PIN） =====
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
                    budgetErr?.let {
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
                    pinErr?.let {
                        Spacer(Modifier.height(8.dp))
                        Text(it, color = MaterialTheme.colorScheme.error)
                    }
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

/**
 * ✅ rows 的类型是 BillDao 里新增的 BillCsvRow
 * 你需要按我下面提醒把 BillCsvRow + getAllForCsv() 加到 BillDao.kt
 */
private fun buildCsv(rows: List<com.example.haojizhang.data.local.dao.BillCsvRow>): String {
    fun esc(s: String?): String {
        val t = (s ?: "").replace("\"", "\"\"")
        return "\"$t\""
    }

    val sb = StringBuilder()
    sb.append("id,amount_yuan,type,category,account,note,occurredAt\n")

    rows.forEach { r ->
        val amountYuan = r.amountCent / 100.0
        val typeText = if (r.type == 1) "收入" else "支出"
        sb.append(r.id).append(",")
        sb.append(amountYuan).append(",")
        sb.append(esc(typeText)).append(",")
        sb.append(esc(r.categoryName)).append(",")
        sb.append(esc(r.accountName)).append(",")
        sb.append(esc(r.note)).append(",")
        sb.append(r.occurredAt).append("\n")
    }
    return sb.toString()
}

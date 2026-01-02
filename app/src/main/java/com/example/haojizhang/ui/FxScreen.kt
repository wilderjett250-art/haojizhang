package com.example.haojizhang.ui
import androidx.compose.foundation.text.KeyboardOptions

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier

import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.launch
import java.math.BigDecimal
import java.math.RoundingMode

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FxScreen() {
    val scope = rememberCoroutineScope()

    // 常用币种（你想多加就加）
    val currencies = remember {
        listOf("CNY", "USD", "EUR", "GBP", "JPY", "HKD", "KRW", "AUD", "CAD", "SGD")
    }

    var from by remember { mutableStateOf("USD") }
    var to by remember { mutableStateOf("CNY") }
    var amountText by remember { mutableStateOf("100") }

    var loading by remember { mutableStateOf(false) }
    var resultText by remember { mutableStateOf<String?>(null) }
    var errorText by remember { mutableStateOf<String?>(null) }

    fun parseAmount(): BigDecimal? {
        val t = amountText.trim()
        if (t.isBlank()) return null
        return try {
            BigDecimal(t).setScale(2, RoundingMode.HALF_UP)
        } catch (_: Exception) {
            null
        }
    }

    suspend fun doConvert() {
        errorText = null
        resultText = null

        val amt = parseAmount()
        if (amt == null || amt <= BigDecimal.ZERO) {
            errorText = "请输入正确金额（>0）"
            return
        }
        if (from == to) {
            resultText = "相同币种：$amt $from"
            return
        }

        loading = true
        try {
            val res = FrankfurterApi.convert(
                amount = amt.toPlainString(),
                from = from,
                to = to
            )
            // res.rates[to] 就是换算结果
            val converted = res.rates[to]
            if (converted == null) {
                errorText = "汇率返回为空（可能币种不支持）"
            } else {
                resultText = "${amt.toPlainString()} $from  =  ${format2(converted)} $to\n" +
                        "日期：${res.date}"
            }
        } catch (e: Exception) {
            errorText = "请求失败：${e.message ?: "未知错误"}"
        } finally {
            loading = false
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text("工具：汇率转换", style = MaterialTheme.typography.titleLarge)

        OutlinedTextField(
            value = amountText,
            onValueChange = {
                amountText = it
                errorText = null
            },
            label = { Text("金额") },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
            singleLine = true,
            modifier = Modifier.fillMaxWidth()
        )

        Row(horizontalArrangement = Arrangement.spacedBy(12.dp), modifier = Modifier.fillMaxWidth()) {
            CurrencyDropdown(
                title = "从",
                value = from,
                options = currencies,
                onChange = { from = it; errorText = null },
                modifier = Modifier.weight(1f)
            )
            CurrencyDropdown(
                title = "到",
                value = to,
                options = currencies,
                onChange = { to = it; errorText = null },
                modifier = Modifier.weight(1f)
            )
        }

        Row(horizontalArrangement = Arrangement.spacedBy(12.dp), modifier = Modifier.fillMaxWidth()) {
            OutlinedButton(
                onClick = {
                    val tmp = from
                    from = to
                    to = tmp
                },
                modifier = Modifier.weight(1f)
            ) { Text("交换") }

            Button(
                onClick = { scope.launch { doConvert() } },
                enabled = !loading,
                modifier = Modifier.weight(1f)
            ) { Text(if (loading) "换算中..." else "换算") }
        }

        errorText?.let {
            Text(it, color = MaterialTheme.colorScheme.error)
        }

        resultText?.let {
            Card(Modifier.fillMaxWidth()) {
                Column(Modifier.padding(16.dp)) {
                    Text("结果", style = MaterialTheme.typography.titleMedium)
                    Spacer(Modifier.height(8.dp))
                    Text(it)
                }
            }
        }

        Spacer(Modifier.height(8.dp))
        Text(
            "数据来源：Frankfurter（欧央行参考汇率）。\n" +
                    "注意：周末/节假日可能返回最近一个工作日的日期。",
            style = MaterialTheme.typography.bodySmall
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun CurrencyDropdown(
    title: String,
    value: String,
    options: List<String>,
    onChange: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    var expanded by remember { mutableStateOf(false) }

    ExposedDropdownMenuBox(
        expanded = expanded,
        onExpandedChange = { expanded = !expanded },
        modifier = modifier
    ) {
        OutlinedTextField(
            value = value,
            onValueChange = {},
            readOnly = true,
            label = { Text(title) },
            modifier = Modifier
                .menuAnchor()
                .fillMaxWidth()
        )
        ExposedDropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false }
        ) {
            options.forEach { c ->
                DropdownMenuItem(
                    text = { Text(c) },
                    onClick = {
                        onChange(c)
                        expanded = false
                    }
                )
            }
        }
    }
}

private fun format2(num: Double): String {
    return try {
        BigDecimal(num).setScale(2, RoundingMode.HALF_UP).toPlainString()
    } catch (_: Exception) {
        num.toString()
    }
}

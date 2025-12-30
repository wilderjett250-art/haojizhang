package com.example.haojizhang.ui

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.example.haojizhang.data.local.db.DbProvider
import com.example.haojizhang.data.util.DateUtils
import java.util.Calendar

@Composable
fun HomeScreen() {
    val context = LocalContext.current
    val db = remember { DbProvider.get(context) }

    // 本月时间范围
    val (startMillis, endMillis) = remember { currentMonthRangeMillis() }
    val yearMonth = remember { DateUtils.currentYearMonth() }

    // 1) 本月收支汇总（分）
    val monthExpenseCent by db.billDao()
        .observeSumByType(0, startMillis, endMillis)
        .collectAsState(initial = 0L)

    val monthIncomeCent by db.billDao()
        .observeSumByType(1, startMillis, endMillis)
        .collectAsState(initial = 0L)

    // 2) 最近 5 笔（从本月账单里取前 5）
    val monthBills by db.billDao()
        .observeBetween(startMillis, endMillis)
        .collectAsState(initial = emptyList())

    val recent5 = remember(monthBills) { monthBills.take(5) }

    // 3) 本月预算
    val budget by db.budgetDao()
        .observeByMonth(yearMonth)
        .collectAsState(initial = null)

    val budgetCent = budget?.limitCent ?: 0L
    val remainingCent = (budgetCent - monthExpenseCent).coerceAtLeast(0L)

    // UI
    LazyColumn(
        modifier = Modifier.fillMaxSize().padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            Text("首页", style = MaterialTheme.typography.titleLarge)
        }

        // 本月收支卡片
        item {
            Card(Modifier.fillMaxWidth()) {
                Column(Modifier.padding(16.dp)) {
                    Text("本月收支汇总（$yearMonth）", style = MaterialTheme.typography.titleMedium)
                    Spacer(Modifier.height(12.dp))

                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text("本月支出")
                        Text("¥${centToYuanText(monthExpenseCent)}")
                    }
                    Spacer(Modifier.height(8.dp))
                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text("本月收入")
                        Text("¥${centToYuanText(monthIncomeCent)}")
                    }
                }
            }
        }

        // 预算卡片
        item {
            Card(Modifier.fillMaxWidth()) {
                Column(Modifier.padding(16.dp)) {
                    Text("本月预算", style = MaterialTheme.typography.titleMedium)
                    Spacer(Modifier.height(12.dp))

                    if (budget == null) {
                        Text("你还没设置本月预算（后续可在「我的」页加入口）")
                    } else {
                        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text("预算上限")
                            Text("¥${centToYuanText(budgetCent)}")
                        }
                        Spacer(Modifier.height(8.dp))
                        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text("已支出")
                            Text("¥${centToYuanText(monthExpenseCent)}")
                        }
                        Spacer(Modifier.height(8.dp))
                        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text("剩余可用")
                            Text("¥${centToYuanText(remainingCent)}")
                        }
                    }
                }
            }
        }

        // 最近 5 笔
        item {
            Card(Modifier.fillMaxWidth()) {
                Column(Modifier.padding(16.dp)) {
                    Text("最近 5 笔", style = MaterialTheme.typography.titleMedium)
                    Spacer(Modifier.height(12.dp))

                    if (recent5.isEmpty()) {
                        Text("本月暂无账单，点右下角 + 新增一笔")
                    }
                }
            }
        }

        if (recent5.isNotEmpty()) {
            items(recent5) { b ->
                Card(Modifier.fillMaxWidth()) {
                    Column(Modifier.padding(12.dp)) {
                        val sign = if (b.type == 0) "-" else "+"
                        Text(
                            text = "${if (b.type == 0) "支出" else "收入"}  $sign¥${centToYuanText(b.amountCent)}",
                            style = MaterialTheme.typography.titleMedium
                        )
                        b.note?.takeIf { it.isNotBlank() }?.let { Text("备注：$it") }
                    }
                }
            }
        }
    }
}

/** 本月 [startMillis, endMillis]（包含本月最后一毫秒） */
private fun currentMonthRangeMillis(): Pair<Long, Long> {
    val start = Calendar.getInstance().apply {
        set(Calendar.DAY_OF_MONTH, 1)
        set(Calendar.HOUR_OF_DAY, 0)
        set(Calendar.MINUTE, 0)
        set(Calendar.SECOND, 0)
        set(Calendar.MILLISECOND, 0)
    }.timeInMillis

    val end = Calendar.getInstance().apply {
        add(Calendar.MONTH, 1)
        set(Calendar.DAY_OF_MONTH, 1)
        set(Calendar.HOUR_OF_DAY, 0)
        set(Calendar.MINUTE, 0)
        set(Calendar.SECOND, 0)
        set(Calendar.MILLISECOND, 0)
    }.timeInMillis - 1

    return start to end
}

/** 分 -> 元（字符串），保留 2 位小数 */
private fun centToYuanText(cent: Long): String {
    val abs = kotlin.math.abs(cent)
    val yuan = abs / 100
    val fen = abs % 100
    return "$yuan.${fen.toString().padStart(2, '0')}"
}

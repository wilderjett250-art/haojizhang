package com.example.haojizhang.ui
import kotlinx.coroutines.flow.firstOrNull

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
import kotlin.math.max

@Composable
fun InsightsScreen() {
    val context = LocalContext.current
    val db = remember { DbProvider.get(context) }

    val (startMillis, endMillis) = remember { currentMonthRangeMillis() }
    val yearMonth = remember { DateUtils.currentYearMonth() }

    // 本月收支（分）
    val expenseCent by db.billDao()
        .observeSumByType(0, startMillis, endMillis)
        .collectAsState(initial = 0L)

    val incomeCent by db.billDao()
        .observeSumByType(1, startMillis, endMillis)
        .collectAsState(initial = 0L)

    // 支出分类聚合：categoryId + totalCent
    val catAgg by db.billDao()
        .observeCategoryAgg(0, startMillis, endMillis)
        .collectAsState(initial = emptyList())

    // 分类列表（支出类型：0）
    val categories by db.categoryDao()
        .observeVisibleByType(0)
        .collectAsState(initial = emptyList())

    // id -> name 映射
    val catNameMap = remember(categories) { categories.associateBy({ it.id }, { it.name }) }

    val top5 = remember(catAgg) { catAgg.take(5) }
    val totalExpense = expenseCent
    val maxTop = remember(top5) { top5.maxOfOrNull { it.totalCent } ?: 0L }

    LazyColumn(
        modifier = Modifier.fillMaxSize().padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            Text("洞察", style = MaterialTheme.typography.titleLarge)
        }

        item {
            Card(Modifier.fillMaxWidth()) {
                Column(Modifier.padding(16.dp)) {
                    Text("本月概览（$yearMonth）", style = MaterialTheme.typography.titleMedium)
                    Spacer(Modifier.height(12.dp))
                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text("本月支出")
                        Text("¥${centToYuanText(expenseCent)}")
                    }
                    Spacer(Modifier.height(8.dp))
                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text("本月收入")
                        Text("¥${centToYuanText(incomeCent)}")
                    }
                }
            }
        }

        item {
            Card(Modifier.fillMaxWidth()) {
                Column(Modifier.padding(16.dp)) {
                    Text("支出分类 Top 5", style = MaterialTheme.typography.titleMedium)
                    Spacer(Modifier.height(8.dp))
                    Text("按金额从高到低（本月）", style = MaterialTheme.typography.bodyMedium)
                }
            }
        }

        if (top5.isEmpty()) {
            item {
                Text("本月暂无支出数据，先记一笔支出再来看分类排行。")
            }
        } else {
            items(top5) { row ->
                val name = catNameMap[row.keyId] ?: "未知分类(${row.keyId})"
                val percent = if (totalExpense <= 0) 0 else ((row.totalCent * 100) / totalExpense).toInt()
                val progress = if (maxTop <= 0) 0f else row.totalCent.toFloat() / maxTop.toFloat()

                Card(Modifier.fillMaxWidth()) {
                    Column(Modifier.padding(12.dp)) {
                        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text(name, style = MaterialTheme.typography.titleMedium)
                            Text("¥${centToYuanText(row.totalCent)}")
                        }
                        Spacer(Modifier.height(6.dp))
                        LinearProgressIndicator(progress = progress, modifier = Modifier.fillMaxWidth())
                        Spacer(Modifier.height(4.dp))
                        Text("占比：$percent%", style = MaterialTheme.typography.bodySmall)
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

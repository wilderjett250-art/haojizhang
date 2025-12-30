package com.example.haojizhang.ui

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.example.haojizhang.data.local.dao.CategoryAggRow
import com.example.haojizhang.data.local.db.DbProvider
import com.example.haojizhang.data.local.db.HaoJizhangDatabase
import com.example.haojizhang.data.util.DateUtils
import kotlinx.coroutines.flow.firstOrNull
import java.util.Calendar

@Composable
fun InsightsScreen() {
    val context = LocalContext.current
    val db = remember { DbProvider.get(context) }

    val (startMillis, endMillis) = remember { currentMonthRangeMillis() }
    val yearMonth = remember { DateUtils.currentYearMonth() } // ✅ 只保留这一处

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

    // 本月预算（可选）
    val budget by db.budgetDao().observeByMonth(yearMonth).collectAsState(initial = null)

    // 最近3个月平均支出（可选：用来判断偏高/偏低）
    val avg3MonthExpenseCent by rememberAvgExpenseLast3Months(db)

    LazyColumn(
        modifier = Modifier.fillMaxSize().padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item { Text("洞察", style = MaterialTheme.typography.titleLarge) }

        // ✅ 自动分析
        item {
            val analysis = analyzeSpendingLocal(
                monthExpenseCent = expenseCent,
                monthIncomeCent = incomeCent,
                budgetCent = budget?.limitCent,
                avg3MonthExpenseCent = avg3MonthExpenseCent,
                topCategoryShare = topCategoryShare(catAgg, expenseCent)
            )

            Card(Modifier.fillMaxWidth()) {
                Column(Modifier.padding(16.dp)) {
                    Text("自动分析", style = MaterialTheme.typography.titleMedium)
                    Spacer(Modifier.height(10.dp))
                    Text(analysis.title, style = MaterialTheme.typography.titleLarge)
                    Spacer(Modifier.height(6.dp))
                    Text(analysis.detail, style = MaterialTheme.typography.bodyMedium)

                    Spacer(Modifier.height(10.dp))
                    analysis.suggestions.forEach { s -> Text("• $s") }
                }
            }
        }

        // ✅ 本月概览
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
                    budget?.let {
                        Spacer(Modifier.height(8.dp))
                        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text("本月预算")
                            Text("¥${centToYuanText(it.limitCent)}")
                        }
                    }
                }
            }
        }

        // ✅ Top5
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
            item { Text("本月暂无支出数据，先记一笔支出再来看分类排行。") }
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

private data class SpendingAnalysis(
    val title: String,
    val detail: String,
    val suggestions: List<String>
)

/** Top1 分类占本月支出的比例（0~1），没有数据返回 null */
private fun topCategoryShare(catAgg: List<CategoryAggRow>, totalExpenseCent: Long): Double? {
    if (totalExpenseCent <= 0) return null
    val top = catAgg.maxOfOrNull { it.totalCent } ?: return null
    return top.toDouble() / totalExpenseCent.toDouble()
}

private fun analyzeSpendingLocal(
    monthExpenseCent: Long,
    monthIncomeCent: Long,
    budgetCent: Long?,
    avg3MonthExpenseCent: Long?,
    topCategoryShare: Double?
): SpendingAnalysis {
    val title: String
    val basis = mutableListOf<String>()
    val suggestions = mutableListOf<String>()

    if (budgetCent != null && budgetCent > 0) {
        val r = monthExpenseCent.toDouble() / budgetCent.toDouble()
        when {
            r >= 1.0 -> {
                title = "本月花费偏高（超预算）"
                basis += "支出已超过预算（使用 ${(r * 100).toInt()}%）"
                suggestions += "优先减少非必要消费（餐饮/娱乐/购物）"
                suggestions += "把Top分类作为第一优化目标"
                suggestions += "必要时调整预算更贴近实际"
            }
            r >= 0.85 -> {
                title = "本月花费偏高（接近超预算）"
                basis += "预算使用接近上限（使用 ${(r * 100).toInt()}%）"
                suggestions += "剩余天数控制可变支出"
                suggestions += "避免冲动购物"
                suggestions += "关注Top分类是否异常偏高"
            }
            r <= 0.40 -> {
                title = "本月花费偏低（明显低于预算）"
                basis += "预算使用偏低（使用 ${(r * 100).toInt()}%）"
                suggestions += "检查是否有漏记（现金/小额）"
                suggestions += "如果在攒钱，本月很好"
                suggestions += "预算可调得更贴合实际"
            }
            else -> {
                title = "本月花费正常（预算范围内）"
                basis += "预算使用合理（使用 ${(r * 100).toInt()}%）"
                suggestions += "保持记账习惯"
                suggestions += "关注Top分类的结构变化"
                suggestions += "大额消费记备注更利于回顾"
            }
        }
    } else if (avg3MonthExpenseCent != null && avg3MonthExpenseCent > 0) {
        val r = monthExpenseCent.toDouble() / avg3MonthExpenseCent.toDouble()
        when {
            r >= 1.25 -> {
                title = "本月花费偏高（高于近3月平均）"
                basis += "本月支出约为近3月均值的 ${(r * 100).toInt()}%"
                suggestions += "检查是否有一次性大额支出"
                suggestions += "从Top分类先压一压"
                suggestions += "建议设置预算，预警更明确"
            }
            r <= 0.75 -> {
                title = "本月花费偏低（低于近3月平均）"
                basis += "本月支出约为近3月均值的 ${(r * 100).toInt()}%"
                suggestions += "确认是否漏记"
                suggestions += "如果在控支出，本月不错"
                suggestions += "建议设置预算，洞察更准"
            }
            else -> {
                title = "本月花费正常（接近近3月平均）"
                basis += "与近3月平均接近"
                suggestions += "继续保持规律记账"
                suggestions += "建议设置预算"
                suggestions += "关注Top分类占比变化"
            }
        }
    } else {
        title = "数据不足，暂无法判断"
        basis += "历史样本较少/本月数据不足"
        suggestions += "先记录一段时间再来看"
        suggestions += "建议在「我的」设置预算"
        suggestions += "持续记录越久越准"
    }

    if (monthIncomeCent > 0 && monthExpenseCent > monthIncomeCent) {
        basis += "支出高于收入（现金流压力）"
        suggestions += "尽量让支出不超过收入"
    }

    if (topCategoryShare != null) {
        val p = (topCategoryShare * 100).toInt()
        if (p >= 60) {
            basis += "Top分类占比过高（约 ${p}%）"
            suggestions += "Top分类占比过高，建议设上限"
        } else if (p >= 45) {
            basis += "Top分类占比偏高（约 ${p}%）"
        }
    }

    val detail = buildString {
        append("本月支出 ¥${centToYuanText(monthExpenseCent)}，本月收入 ¥${centToYuanText(monthIncomeCent)}。")
        if (budgetCent != null && budgetCent > 0) append(" 预算 ¥${centToYuanText(budgetCent)}。")
        append("\n依据：")
        append(basis.joinToString("；"))
    }

    return SpendingAnalysis(
        title = title,
        detail = detail,
        suggestions = suggestions.distinct().take(3)
    )
}

/** 最近 3 个月平均支出（分）；取每月支出 Flow 的 firstOrNull() */
@Composable
private fun rememberAvgExpenseLast3Months(db: HaoJizhangDatabase): State<Long?> {
    return produceState<Long?>(initialValue = null, db) {
        value = try {
            val cal = Calendar.getInstance()
            val list = mutableListOf<Long>()
            repeat(3) {
                val (s, e) = monthRangeMillis(cal)
                val sum = db.billDao().observeSumByType(0, s, e).firstOrNull() ?: 0L
                list += sum
                cal.add(Calendar.MONTH, -1)
            }
            if (list.isEmpty()) null else (list.sum() / list.size)
        } catch (_: Exception) {
            null
        }
    }
}

private fun monthRangeMillis(cal: Calendar): Pair<Long, Long> {
    val start = (cal.clone() as Calendar).apply {
        set(Calendar.DAY_OF_MONTH, 1)
        set(Calendar.HOUR_OF_DAY, 0)
        set(Calendar.MINUTE, 0)
        set(Calendar.SECOND, 0)
        set(Calendar.MILLISECOND, 0)
    }.timeInMillis

    val end = (cal.clone() as Calendar).apply {
        add(Calendar.MONTH, 1)
        set(Calendar.DAY_OF_MONTH, 1)
        set(Calendar.HOUR_OF_DAY, 0)
        set(Calendar.MINUTE, 0)
        set(Calendar.SECOND, 0)
        set(Calendar.MILLISECOND, 0)
    }.timeInMillis - 1

    return start to end
}

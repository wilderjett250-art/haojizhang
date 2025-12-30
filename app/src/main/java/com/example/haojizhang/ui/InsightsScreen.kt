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
import com.example.haojizhang.data.util.Prefs
import kotlinx.coroutines.flow.firstOrNull
import java.util.Calendar
import kotlin.math.roundToInt

// ✅ 不要 private（否则别的地方/同文件的引用会乱）
data class SpendingAnalysis(
    val title: String,
    val detail: String,
    val suggestions: List<String>
)

@Composable
fun InsightsScreen() {
    val context = LocalContext.current
    val db = remember { DbProvider.get(context) }

    val (startMillis, endMillis) = remember { currentMonthRangeMillis() }
    val yearMonth = remember { DateUtils.currentYearMonth() }

    // ✅ 从“我的-预算规则”读取阈值（百分比 -> 小数）
    val warnHigh = remember { Prefs.getWarnHigh(context) }       // 0.85
    val warnLow = remember { Prefs.getWarnLow(context) }         // 0.40
    val topShareWarn = remember { Prefs.getTopShare(context) }   // 0.60

    val expenseCent by db.billDao()
        .observeSumByType(0, startMillis, endMillis)
        .collectAsState(initial = 0L)

    val incomeCent by db.billDao()
        .observeSumByType(1, startMillis, endMillis)
        .collectAsState(initial = 0L)

    val catAgg by db.billDao()
        .observeCategoryAgg(0, startMillis, endMillis)
        .collectAsState(initial = emptyList())

    val categories by db.categoryDao()
        .observeVisibleByType(0)
        .collectAsState(initial = emptyList())

    val catNameMap = remember(categories) { categories.associateBy({ it.id }, { it.name }) }

    val top5 = remember(catAgg) { catAgg.take(5) }
    val maxTop = remember(top5) { top5.maxOfOrNull { it.totalCent } ?: 0L }

    val budget by db.budgetDao().observeByMonth(yearMonth).collectAsState(initial = null)
    val avg3MonthExpenseCent by rememberAvgExpenseLast3Months(db)

    // ✅ 这里一定返回 SpendingAnalysis，下面才能 analysis.title/detail/suggestions
    val analysis = remember(expenseCent, incomeCent, budget, avg3MonthExpenseCent, catAgg, warnHigh, warnLow, topShareWarn) {
        analyzeSpendingLocal(
            monthExpenseCent = expenseCent,
            monthIncomeCent = incomeCent,
            budgetCent = budget?.limitCent,
            avg3MonthExpenseCent = avg3MonthExpenseCent,
            topCategoryShare = topCategoryShare(catAgg, expenseCent),
            warnHigh = warnHigh,
            warnLow = warnLow,
            topShareWarn = topShareWarn
        )
    }

    LazyColumn(
        modifier = Modifier.fillMaxSize().padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item { Text("洞察", style = MaterialTheme.typography.titleLarge) }

        item {
            Card(Modifier.fillMaxWidth()) {
                Column(Modifier.padding(16.dp)) {
                    Text("自动分析", style = MaterialTheme.typography.titleMedium)
                    Spacer(Modifier.height(10.dp))

                    // ✅ 这里必须是 String，歧义就没了
                    Text(text = analysis.title, style = MaterialTheme.typography.titleLarge)
                    Spacer(Modifier.height(6.dp))
                    Text(text = analysis.detail, style = MaterialTheme.typography.bodyMedium)

                    Spacer(Modifier.height(10.dp))
                    analysis.suggestions.forEach { s: String ->
                        Text(text = "• $s")
                    }
                }
            }
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
                val totalExpense = expenseCent
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

private fun centToYuanText(cent: Long): String {
    val abs = kotlin.math.abs(cent)
    val yuan = abs / 100
    val fen = abs % 100
    return "$yuan.${fen.toString().padStart(2, '0')}"
}

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
    topCategoryShare: Double?,
    warnHigh: Float,
    warnLow: Float,
    topShareWarn: Float
): SpendingAnalysis {
    val basis = mutableListOf<String>()
    val suggestions = mutableListOf<String>()
    val title: String

    if (budgetCent != null && budgetCent > 0) {
        val r = monthExpenseCent.toDouble() / budgetCent.toDouble()
        val pct = (r * 100).roundToInt()
        when {
            r >= 1.0 -> {
                title = "本月花费偏高（超预算）"
                basis += "支出已超过预算（使用 ${pct}%）"
                suggestions += "优先减少非必要消费"
                suggestions += "从Top分类先优化"
                suggestions += "必要时调整预算"
            }
            r >= warnHigh.toDouble() -> {
                title = "本月花费偏高（接近超预算）"
                basis += "预算使用接近上限（使用 ${pct}%）"
                suggestions += "控制剩余天数的可变支出"
                suggestions += "避免冲动购物"
                suggestions += "关注Top分类是否异常"
            }
            r <= warnLow.toDouble() -> {
                title = "本月花费偏低（明显低于预算）"
                basis += "预算使用偏低（使用 ${pct}%）"
                suggestions += "检查是否漏记（现金/小额）"
                suggestions += "如果在攒钱，本月很好"
                suggestions += "预算可调得更贴合实际"
            }
            else -> {
                title = "本月花费正常（预算范围内）"
                basis += "预算使用合理（使用 ${pct}%）"
                suggestions += "保持记账习惯"
                suggestions += "大额消费记备注更利于回顾"
                suggestions += "关注Top分类结构变化"
            }
        }
    } else if (avg3MonthExpenseCent != null && avg3MonthExpenseCent > 0) {
        val r = monthExpenseCent.toDouble() / avg3MonthExpenseCent.toDouble()
        val pct = (r * 100).roundToInt()
        when {
            r >= 1.25 -> {
                title = "本月花费偏高（高于近3月平均）"
                basis += "本月支出约为近3月均值的 ${pct}%"
                suggestions += "检查是否有一次性大额支出"
                suggestions += "从Top分类先压一压"
                suggestions += "建议设置预算"
            }
            r <= 0.75 -> {
                title = "本月花费偏低（低于近3月平均）"
                basis += "本月支出约为近3月均值的 ${pct}%"
                suggestions += "确认是否漏记"
                suggestions += "如果在控支出，本月不错"
                suggestions += "建议设置预算"
            }
            else -> {
                title = "本月花费正常（接近近3月平均）"
                basis += "与近3月平均接近（约 ${pct}%）"
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
        val p = (topCategoryShare * 100).roundToInt()
        if (topCategoryShare >= topShareWarn.toDouble()) {
            basis += "Top分类占比过高（约 ${p}%）"
            suggestions += "Top分类占比过高，建议设上限"
        }
    }

    val detail = buildString {
        append("本月支出 ¥${centToYuanText(monthExpenseCent)}，本月收入 ¥${centToYuanText(monthIncomeCent)}。")
        if (budgetCent != null && budgetCent > 0) append(" 预算 ¥${centToYuanText(budgetCent)}。")
        append("\n依据：")
        append(basis.joinToString("；"))
        append("\n规则：偏高≥${(warnHigh * 100).roundToInt()}%  偏低≤${(warnLow * 100).roundToInt()}%  Top≥${(topShareWarn * 100).roundToInt()}%")
    }

    return SpendingAnalysis(
        title = title,
        detail = detail,
        suggestions = suggestions.distinct().take(3)
    )
}

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

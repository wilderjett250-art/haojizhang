package com.example.haojizhang.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.example.haojizhang.data.local.db.DbProvider
import com.example.haojizhang.data.repository.BillRepository
import java.time.LocalDate
import java.time.ZoneId

@Composable
fun InsightsScreen() {
    val context = LocalContext.current
    val db = remember { DbProvider.get(context) }
    val repo = remember { BillRepository(db.billDao()) }

    val (startMillis, endMillis) = remember { thisMonthRange() }

    val expenseCent by repo.observeSumByType(0, startMillis, endMillis).collectAsState(initial = 0L)
    val incomeCent by repo.observeSumByType(1, startMillis, endMillis).collectAsState(initial = 0L)

    val catsExpense by db.categoryDao().observeVisibleByType(0).collectAsState(initial = emptyList())
    val agg by db.billDao().observeCategoryAgg(0, startMillis, endMillis).collectAsState(initial = emptyList())

    val catMap = remember(catsExpense) { catsExpense.associateBy { it.id } }

    Column(Modifier.fillMaxSize().padding(16.dp)) {
        Text("报表（本月）", style = MaterialTheme.typography.titleLarge)
        Spacer(Modifier.height(12.dp))

        Card(Modifier.fillMaxWidth()) {
            Column(Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                Text("支出：¥${centToYuan(expenseCent)}", style = MaterialTheme.typography.titleMedium)
                Text("收入：¥${centToYuan(incomeCent)}", style = MaterialTheme.typography.titleMedium)
                Text("结余：¥${centToYuan(incomeCent - expenseCent)}", style = MaterialTheme.typography.bodyMedium)
            }
        }

        Spacer(Modifier.height(14.dp))
        Text("支出分类 Top", style = MaterialTheme.typography.titleMedium)
        Spacer(Modifier.height(8.dp))

        if (agg.isEmpty()) {
            Text("本月还没有支出数据。")
            return
        }

        agg.take(6).forEach { row ->
            val name = catMap[row.keyId]?.name ?: "未分类"
            Text("• $name：¥${centToYuan(row.totalCent)}")
            Spacer(Modifier.height(4.dp))
        }
    }
}

private fun thisMonthRange(): Pair<Long, Long> {
    val zone = ZoneId.systemDefault()
    val today = LocalDate.now(zone)
    val start = today.withDayOfMonth(1).atStartOfDay(zone).toInstant().toEpochMilli()
    val nextMonthStart = today.plusMonths(1).withDayOfMonth(1).atStartOfDay(zone).toInstant().toEpochMilli()
    return start to (nextMonthStart - 1)
}

private fun centToYuan(cent: Long): String = String.format("%.2f", cent / 100.0)

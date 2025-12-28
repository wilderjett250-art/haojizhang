package com.example.haojizhang.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
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
fun HomeScreen(
    onAddExpense: () -> Unit,
    onAddIncome: () -> Unit
) {
    val context = LocalContext.current
    val db = remember { DbProvider.get(context) }
    val repo = remember { BillRepository(db.billDao()) }

    val (startMillis, endMillis) = remember { thisMonthRange() }

    val expenseCent by repo.observeSumByType(0, startMillis, endMillis).collectAsState(initial = 0L)
    val incomeCent by repo.observeSumByType(1, startMillis, endMillis).collectAsState(initial = 0L)

    Column(Modifier.fillMaxSize().padding(16.dp)) {
        Text("首页", style = MaterialTheme.typography.titleLarge)
        Spacer(Modifier.height(12.dp))

        Card(Modifier.fillMaxWidth()) {
            Column(Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                Text("本月支出：¥${centToYuan(expenseCent)}", style = MaterialTheme.typography.titleMedium)
                Text("本月收入：¥${centToYuan(incomeCent)}", style = MaterialTheme.typography.titleMedium)
                Text("本月结余：¥${centToYuan(incomeCent - expenseCent)}", style = MaterialTheme.typography.bodyMedium)
            }
        }

        Spacer(Modifier.height(16.dp))

        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            Button(onClick = onAddExpense) { Text("记一笔支出") }
            Button(onClick = onAddIncome) { Text("记一笔收入") }
        }

        Spacer(Modifier.height(18.dp))
        Text("提示：新增时可选择【类型 / 分类 / 账户】；账单页与报表页会自动更新。", style = MaterialTheme.typography.bodyMedium)
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

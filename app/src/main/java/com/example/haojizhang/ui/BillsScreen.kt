package com.example.haojizhang.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Card
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.example.haojizhang.data.local.db.DbProvider
import com.example.haojizhang.data.repository.BillRepository
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.time.format.DateTimeFormatter

@Composable
fun BillsScreen() {
    val context = LocalContext.current
    val db = remember { DbProvider.get(context) }
    val repo = remember { BillRepository(db.billDao()) }

    val (startMillis, endMillis) = remember { thisMonthRange() }

    var filter by remember { mutableStateOf(2) } // 2=全部 0=支出 1=收入

    val bills by repo.observeBillsBetween(startMillis, endMillis).collectAsState(initial = emptyList())
    val accounts by db.accountDao().observeActive().collectAsState(initial = emptyList())
    val catExpense by db.categoryDao().observeVisibleByType(0).collectAsState(initial = emptyList())
    val catIncome by db.categoryDao().observeVisibleByType(1).collectAsState(initial = emptyList())

    val categoryMap = remember(catExpense, catIncome) {
        (catExpense + catIncome).associateBy { it.id }
    }
    val accountMap = remember(accounts) {
        accounts.associateBy { it.id }
    }

    val showList = remember(bills, filter) {
        if (filter == 2) bills else bills.filter { it.type == filter }
    }

    Column(Modifier.fillMaxSize().padding(16.dp)) {
        Text("账单（本月）", style = MaterialTheme.typography.titleLarge)
        Spacer(Modifier.height(10.dp))

        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            FilterChip(selected = filter == 2, onClick = { filter = 2 }, label = { Text("全部") })
            FilterChip(selected = filter == 0, onClick = { filter = 0 }, label = { Text("支出") })
            FilterChip(selected = filter == 1, onClick = { filter = 1 }, label = { Text("收入") })
        }

        Spacer(Modifier.height(12.dp))

        if (showList.isEmpty()) {
            Text("本月还没有记录，去点右下角 + 记一笔。")
            return
        }

        LazyColumn(verticalArrangement = Arrangement.spacedBy(10.dp)) {
            items(showList, key = { it.id }) { b ->
                val sign = if (b.type == 0) "-" else "+"
                val catName = categoryMap[b.categoryId]?.name ?: "未分类"
                val accName = accountMap[b.accountId]?.name ?: "未知账户"
                val timeText = formatTime(b.occurredAt)

                Card(Modifier.fillMaxWidth()) {
                    Column(Modifier.padding(14.dp)) {
                        Text("$sign ¥${centToYuan(b.amountCent)}  ·  $catName", style = MaterialTheme.typography.titleMedium)
                        Spacer(Modifier.height(4.dp))
                        Text("$accName  ·  $timeText", style = MaterialTheme.typography.bodySmall)
                        if (!b.note.isNullOrBlank()) {
                            Spacer(Modifier.height(4.dp))
                            Text(b.note!!, style = MaterialTheme.typography.bodyMedium)
                        }
                    }
                }
            }
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

private fun formatTime(epochMillis: Long): String {
    val zone = ZoneId.systemDefault()
    val dt = Instant.ofEpochMilli(epochMillis).atZone(zone).toLocalDateTime()
    return dt.format(DateTimeFormatter.ofPattern("MM-dd HH:mm"))
}

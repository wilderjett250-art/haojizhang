package com.example.haojizhang.ui

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.room.Room
import com.example.haojizhang.data.local.db.HaoJizhangDatabase
import com.example.haojizhang.data.repository.BillRepository
import java.time.LocalDate
import java.time.ZoneId

@Composable
fun InsightsScreen() {

    val context = LocalContext.current

    val db = remember {
        Room.databaseBuilder(
            context,
            HaoJizhangDatabase::class.java,
            "haojizhang.db"
        ).build()
    }

    val repo = remember { BillRepository(db.billDao()) }

    val now = LocalDate.now()
    val startMillis = now.withDayOfMonth(1)
        .atStartOfDay(ZoneId.systemDefault())
        .toInstant().toEpochMilli()

    val endMillis = now.plusMonths(1)
        .withDayOfMonth(1)
        .atStartOfDay(ZoneId.systemDefault())
        .toInstant().toEpochMilli()

    val categoryStats by repo
        .observeCategoryExpense(startMillis, endMillis)
        .collectAsState(initial = emptyList())

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Text("支出洞察", style = MaterialTheme.typography.titleLarge)
        Spacer(Modifier.height(12.dp))

        if (categoryStats.isEmpty()) {
            Text("本月暂无支出数据")
        } else {
            categoryStats.forEach {
                Text("分类 ${it.keyId}：¥ ${it.totalCent / 100.0}")
                Spacer(Modifier.height(6.dp))
            }
        }
    }
}

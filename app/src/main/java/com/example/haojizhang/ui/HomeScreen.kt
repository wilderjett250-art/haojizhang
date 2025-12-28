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
fun HomeScreen() {

    val context = LocalContext.current

    // ⚠️ 直接构建数据库（不依赖 getInstance）
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

    val expense by repo.observeMonthExpense(startMillis, endMillis)
        .collectAsState(initial = 0L)

    val income by repo.observeMonthIncome(startMillis, endMillis)
        .collectAsState(initial = 0L)

    val balance = income - expense

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Text("本月概览", style = MaterialTheme.typography.titleLarge)
        Spacer(Modifier.height(16.dp))

        Text("支出：¥ ${expense / 100.0}")
        Text("收入：¥ ${income / 100.0}")
        Spacer(Modifier.height(8.dp))
        Text("结余：¥ ${balance / 100.0}", style = MaterialTheme.typography.titleMedium)
    }
}

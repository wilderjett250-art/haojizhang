package com.example.haojizhang.ui

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.platform.LocalContext
import com.example.haojizhang.data.local.db.DbProvider
import com.example.haojizhang.ui.vm.BillsViewModel
import java.util.Calendar
import androidx.compose.runtime.collectAsState

@Composable
fun BillsScreen() {
    val context = LocalContext.current
    val db = remember { DbProvider.get(context) }
    val vm = remember { BillsViewModel(db.billDao()) }

    val startMillis = remember {
        Calendar.getInstance().apply {
            set(Calendar.DAY_OF_MONTH, 1)
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }.timeInMillis
    }
    val endMillis = remember {
        Calendar.getInstance().apply {
            add(Calendar.MONTH, 1)
            set(Calendar.DAY_OF_MONTH, 1)
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }.timeInMillis - 1
    }

    val bills by vm.observeBetween(startMillis, endMillis).collectAsState(initial = emptyList())

    Column(Modifier.fillMaxSize().padding(16.dp)) {
        Text("账单", style = MaterialTheme.typography.titleLarge)
        Spacer(Modifier.height(12.dp))

        if (bills.isEmpty()) {
            Text("本月暂无账单，点右下角 + 新增一笔")
        } else {
            LazyColumn {
                items(bills) { b ->
                    Card(Modifier.fillMaxWidth().padding(vertical = 6.dp)) {
                        Column(Modifier.padding(12.dp)) {
                            val yuan = b.amountCent / 100.0
                            Text(
                                text = (if (b.type == 0) "支出" else "收入") + "  ¥$yuan",
                                style = MaterialTheme.typography.titleMedium
                            )
                            b.note?.let { Text("备注：$it") }
                        }
                    }
                }
            }
        }
    }
}

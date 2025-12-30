@file:OptIn(ExperimentalMaterial3Api::class)

package com.example.haojizhang.ui

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import com.example.haojizhang.data.local.db.DbProvider
import com.example.haojizhang.data.repository.BillRepository
import com.example.haojizhang.ui.vm.AddBillViewModel
import kotlinx.coroutines.launch

@Composable
fun AddBillScreen(navController: NavHostController) {
    val context = LocalContext.current
    val db = remember { DbProvider.get(context) }

    val repo = remember { BillRepository(db.billDao()) }
    val vm = remember { AddBillViewModel(repo) }

    var amount by remember { mutableStateOf("") }
    var note by remember { mutableStateOf("") }
    var type by remember { mutableStateOf(0) } // 0=支出 1=收入

    val categories by db.categoryDao()
        .observeVisibleByType(type)
        .collectAsState(initial = emptyList())

    val accounts by db.accountDao()
        .observeActive()
        .collectAsState(initial = emptyList())

    var selectedCategoryId by remember { mutableStateOf<Long?>(null) }
    var selectedAccountId by remember { mutableStateOf<Long?>(null) }

    var catExpanded by remember { mutableStateOf(false) }
    var accExpanded by remember { mutableStateOf(false) }

    var errorMsg by remember { mutableStateOf<String?>(null) }
    val scope = rememberCoroutineScope()

    // 默认选择第一项（当数据加载完成时）
    LaunchedEffect(type, categories) {
        if (selectedCategoryId == null || categories.none { it.id == selectedCategoryId }) {
            selectedCategoryId = categories.firstOrNull()?.id
        }
    }
    LaunchedEffect(accounts) {
        if (selectedAccountId == null || accounts.none { it.id == selectedAccountId }) {
            selectedAccountId = accounts.firstOrNull()?.id
        }
    }

    Column(
        modifier = Modifier.fillMaxSize().padding(16.dp)
    ) {
        Text("新增账单", style = MaterialTheme.typography.titleLarge)
        Spacer(Modifier.height(16.dp))

        OutlinedTextField(
            value = amount,
            onValueChange = { amount = it },
            label = { Text("金额（元）") },
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(Modifier.height(12.dp))

        Row {
            FilterChip(
                selected = type == 0,
                onClick = { type = 0; errorMsg = null },
                label = { Text("支出") }
            )
            Spacer(Modifier.width(8.dp))
            FilterChip(
                selected = type == 1,
                onClick = { type = 1; errorMsg = null },
                label = { Text("收入") }
            )
        }

        Spacer(Modifier.height(12.dp))

        // 分类下拉
        ExposedDropdownMenuBox(
            expanded = catExpanded,
            onExpandedChange = { catExpanded = !catExpanded }
        ) {
            val currentCatName = categories.firstOrNull { it.id == selectedCategoryId }?.name ?: "请选择分类"

            OutlinedTextField(
                value = currentCatName,
                onValueChange = {},
                readOnly = true,
                label = { Text("分类") },
                modifier = Modifier.menuAnchor().fillMaxWidth()
            )

            ExposedDropdownMenu(
                expanded = catExpanded,
                onDismissRequest = { catExpanded = false }
            ) {
                categories.forEach { c ->
                    DropdownMenuItem(
                        text = { Text(c.name) },
                        onClick = {
                            selectedCategoryId = c.id
                            catExpanded = false
                            errorMsg = null
                        }
                    )
                }
            }
        }

        Spacer(Modifier.height(12.dp))

        // 账户下拉
        ExposedDropdownMenuBox(
            expanded = accExpanded,
            onExpandedChange = { accExpanded = !accExpanded }
        ) {
            val currentAccName = accounts.firstOrNull { it.id == selectedAccountId }?.name ?: "请选择账户"

            OutlinedTextField(
                value = currentAccName,
                onValueChange = {},
                readOnly = true,
                label = { Text("账户") },
                modifier = Modifier.menuAnchor().fillMaxWidth()
            )

            ExposedDropdownMenu(
                expanded = accExpanded,
                onDismissRequest = { accExpanded = false }
            ) {
                accounts.forEach { a ->
                    DropdownMenuItem(
                        text = { Text(a.name) },
                        onClick = {
                            selectedAccountId = a.id
                            accExpanded = false
                            errorMsg = null
                        }
                    )
                }
            }
        }

        Spacer(Modifier.height(12.dp))

        OutlinedTextField(
            value = note,
            onValueChange = { note = it },
            label = { Text("备注（可选）") },
            modifier = Modifier.fillMaxWidth()
        )

        errorMsg?.let {
            Spacer(Modifier.height(12.dp))
            Text(it, color = MaterialTheme.colorScheme.error)
        }

        Spacer(Modifier.height(24.dp))

        Button(
            onClick = {
                scope.launch {
                    val err = vm.saveBill(
                        amountText = amount,
                        type = type,
                        categoryId = selectedCategoryId,
                        accountId = selectedAccountId,
                        note = note
                    )
                    errorMsg = err
                    if (err == null) navController.popBackStack()
                }
            },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("保存账单")
        }
    }
}

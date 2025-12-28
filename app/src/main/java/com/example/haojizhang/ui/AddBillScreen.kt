package com.example.haojizhang.ui

import android.widget.Toast
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.example.haojizhang.data.local.db.DbProvider
import com.example.haojizhang.data.repository.BillRepository
import com.example.haojizhang.ui.vm.AddBillViewModel
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddBillScreen(
    initialType: Int = 0,
    onBack: () -> Unit
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    val db = remember { DbProvider.get(context) }
    val repo = remember { BillRepository(db.billDao()) }
    val vm = remember { AddBillViewModel(repo) }

    var amountText by remember { mutableStateOf("") }
    var noteText by remember { mutableStateOf("") }

    var type by remember { mutableStateOf(if (initialType == 1) 1 else 0) }

    val categories by db.categoryDao().observeVisibleByType(type).collectAsState(initial = emptyList())
    val accounts by db.accountDao().observeActive().collectAsState(initial = emptyList())

    var selectedCategoryId by remember { mutableStateOf<Long?>(null) }
    var selectedAccountId by remember { mutableStateOf<Long?>(null) }

    // 切换类型时，重置分类选择
    LaunchedEffect(type) {
        selectedCategoryId = null
    }

    // 默认选中第一项（表为空时不会选）
    LaunchedEffect(categories) {
        if (selectedCategoryId == null && categories.isNotEmpty()) {
            selectedCategoryId = categories.first().id
        }
    }
    LaunchedEffect(accounts) {
        if (selectedAccountId == null && accounts.isNotEmpty()) {
            selectedAccountId = accounts.first().id
        }
    }

    Column(Modifier.fillMaxSize().padding(16.dp)) {
        Text("新增账单", style = MaterialTheme.typography.titleLarge)
        Spacer(Modifier.height(12.dp))

        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            FilterChip(
                selected = type == 0,
                onClick = { type = 0 },
                label = { Text("支出") }
            )
            FilterChip(
                selected = type == 1,
                onClick = { type = 1 },
                label = { Text("收入") }
            )
        }

        Spacer(Modifier.height(12.dp))

        OutlinedTextField(
            value = amountText,
            onValueChange = { amountText = it },
            label = { Text("金额（元）") },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
        )

        Spacer(Modifier.height(12.dp))

        // 分类下拉
        var catExpanded by remember { mutableStateOf(false) }
        val selectedCatName = categories.firstOrNull { it.id == selectedCategoryId }?.name ?: "请选择分类"

        ExposedDropdownMenuBox(
            expanded = catExpanded,
            onExpandedChange = { catExpanded = !catExpanded }
        ) {
            TextField(
                value = selectedCatName,
                onValueChange = {},
                readOnly = true,
                label = { Text("分类") },
                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = catExpanded) },
                modifier = Modifier.fillMaxWidth().menuAnchor()
            )
            androidx.compose.material3.ExposedDropdownMenu(
                expanded = catExpanded,
                onDismissRequest = { catExpanded = false }
            ) {
                categories.forEach { c ->
                    DropdownMenuItem(
                        text = { Text(c.name) },
                        onClick = {
                            selectedCategoryId = c.id
                            catExpanded = false
                        }
                    )
                }
            }
        }

        Spacer(Modifier.height(12.dp))

        // 账户下拉
        var accExpanded by remember { mutableStateOf(false) }
        val selectedAccName = accounts.firstOrNull { it.id == selectedAccountId }?.name ?: "请选择账户"

        ExposedDropdownMenuBox(
            expanded = accExpanded,
            onExpandedChange = { accExpanded = !accExpanded }
        ) {
            TextField(
                value = selectedAccName,
                onValueChange = {},
                readOnly = true,
                label = { Text("账户") },
                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = accExpanded) },
                modifier = Modifier.fillMaxWidth().menuAnchor()
            )
            androidx.compose.material3.ExposedDropdownMenu(
                expanded = accExpanded,
                onDismissRequest = { accExpanded = false }
            ) {
                accounts.forEach { a ->
                    DropdownMenuItem(
                        text = { Text(a.name) },
                        onClick = {
                            selectedAccountId = a.id
                            accExpanded = false
                        }
                    )
                }
            }
        }

        Spacer(Modifier.height(12.dp))

        OutlinedTextField(
            value = noteText,
            onValueChange = { noteText = it },
            label = { Text("备注（可选）") },
            modifier = Modifier.fillMaxWidth(),
            singleLine = false,
            minLines = 2
        )

        Spacer(Modifier.height(18.dp))

        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            Button(onClick = onBack) { Text("返回") }
            Button(
                onClick = {
                    scope.launch {
                        val err = vm.saveBill(
                            amountText = amountText,
                            type = type,
                            categoryId = selectedCategoryId,
                            accountId = selectedAccountId,
                            note = noteText
                        )
                        if (err != null) {
                            Toast.makeText(context, err, Toast.LENGTH_SHORT).show()
                        } else {
                            Toast.makeText(context, "已保存", Toast.LENGTH_SHORT).show()
                            onBack()
                        }
                    }
                }
            ) { Text("保存") }
        }
    }
}

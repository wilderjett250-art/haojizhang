@file:OptIn(ExperimentalMaterial3Api::class)

package com.example.haojizhang.ui

import android.graphics.Bitmap
import android.graphics.ImageDecoder
import android.net.Uri
import android.os.Build
import android.provider.MediaStore
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import com.example.haojizhang.data.local.db.DbProvider
import com.example.haojizhang.data.repository.BillRepository
import com.example.haojizhang.data.repository.OcrRepository
import com.example.haojizhang.ui.vm.AddBillViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

@Composable
fun AddBillScreen(navController: NavHostController) {
    val context = LocalContext.current
    val db = remember { DbProvider.get(context) }

    val repo = remember { BillRepository(db.billDao()) }
    val vm = remember { AddBillViewModel(repo) }

    // ===== OCR：这里填你自己的百度 Key（课程作业可先写死；正式项目应放后端）=====
    // TODO: 替换为你百度控制台的 API Key / Secret Key
    val ocrRepo = remember {
        OcrRepository(
            apiKey = "YOUR_BAIDU_API_KEY",
            secretKey = "YOUR_BAIDU_SECRET_KEY"
        )
    }

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

    // ===== OCR 状态 =====
    var ocrLoading by remember { mutableStateOf(false) }

    // 拍照：返回 Bitmap（不需要文件权限）
    val cameraPreviewLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.TakePicturePreview()
    ) { bmp: Bitmap? ->
        if (bmp == null) return@rememberLauncherForActivityResult
        scope.launch {
            runOcrFill(bmp, ocrRepo,
                onLoading = { ocrLoading = it },
                onSuccess = { amt, nt ->
                    if (!amt.isNullOrBlank()) amount = amt
                    if (!nt.isNullOrBlank()) note = nt
                    errorMsg = null
                },
                onError = { msg -> errorMsg = msg }
            )
        }
    }

    // 相册选图：返回 Uri
    val pickImageLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        if (uri == null) return@rememberLauncherForActivityResult
        scope.launch {
            val bmp = withContext(Dispatchers.IO) { loadBitmapFromUri(context, uri) }
            if (bmp == null) {
                errorMsg = "图片读取失败"
                return@launch
            }
            runOcrFill(bmp, ocrRepo,
                onLoading = { ocrLoading = it },
                onSuccess = { amt, nt ->
                    if (!amt.isNullOrBlank()) amount = amt
                    if (!nt.isNullOrBlank()) note = nt
                    errorMsg = null
                },
                onError = { msg -> errorMsg = msg }
            )
        }
    }

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

        // ===== NEW：OCR 区域（不影响你原本保存逻辑）=====
        Row(horizontalArrangement = Arrangement.spacedBy(10.dp), modifier = Modifier.fillMaxWidth()) {
            OutlinedButton(
                onClick = { cameraPreviewLauncher.launch(null) },
                enabled = !ocrLoading,
                modifier = Modifier.weight(1f)
            ) { Text(if (ocrLoading) "识别中..." else "拍照识别小票") }

            OutlinedButton(
                onClick = { pickImageLauncher.launch("image/*") },
                enabled = !ocrLoading,
                modifier = Modifier.weight(1f)
            ) { Text(if (ocrLoading) "识别中..." else "相册识别小票") }
        }

        Spacer(Modifier.height(12.dp))

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
            enabled = !ocrLoading,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("保存账单")
        }
    }
}

// ====== OCR 帮助函数：后台识别后填充 ======
private suspend fun runOcrFill(
    bmp: Bitmap,
    repo: OcrRepository,
    onLoading: (Boolean) -> Unit,
    onSuccess: (amount: String?, note: String?) -> Unit,
    onError: (String) -> Unit
) {
    onLoading(true)
    try {
        val r = withContext(Dispatchers.IO) { repo.recognizeReceipt(bmp) }
        onSuccess(r.amountYuan, r.note)
    } catch (e: Exception) {
        onError("OCR 识别失败：${e.message}")
    } finally {
        onLoading(false)
    }
}

private fun loadBitmapFromUri(context: android.content.Context, uri: Uri): Bitmap? {
    return try {
        if (Build.VERSION.SDK_INT >= 28) {
            val src = ImageDecoder.createSource(context.contentResolver, uri)
            ImageDecoder.decodeBitmap(src)
        } else {
            @Suppress("DEPRECATION")
            MediaStore.Images.Media.getBitmap(context.contentResolver, uri)
        }
    } catch (_: Exception) {
        null
    }
}

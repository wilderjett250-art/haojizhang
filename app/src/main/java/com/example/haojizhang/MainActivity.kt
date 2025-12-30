package com.example.haojizhang

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.lifecycle.lifecycleScope
import com.example.haojizhang.data.local.seed.SeedDataRunner
import com.example.haojizhang.ui.AppRoot
import com.example.haojizhang.ui.theme.HaojizhangTheme
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // ✅ 启动时初始化默认数据（不阻塞UI线程）
        lifecycleScope.launch(Dispatchers.IO) {
            SeedDataRunner.ensureSeeded(this@MainActivity)
        }

        setContent {
            HaojizhangTheme {
                AppRoot()
            }
        }
    }
}

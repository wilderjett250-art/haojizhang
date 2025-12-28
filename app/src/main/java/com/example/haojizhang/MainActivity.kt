package com.example.haojizhang

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.lifecycle.lifecycleScope
import com.example.haojizhang.data.seed.SeedDataRunner
import com.example.haojizhang.ui.AppRoot
import com.example.haojizhang.ui.theme.HaoJizhangTheme
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        lifecycleScope.launch(Dispatchers.IO) {
            SeedDataRunner.ensure(applicationContext)
        }

        setContent {
            HaoJizhangTheme {
                AppRoot()
            }
        }
    }
}

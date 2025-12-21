package com.example.haojizhang

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import com.example.haojizhang.ui.AppRoot
import com.example.haojizhang.ui.theme.HaoJiZhangTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            HaoJiZhangTheme {
                AppRoot()
            }
        }
    }
}

package com.example.haojizhang.data.util

import java.text.SimpleDateFormat
import java.util.*

object DateUtils {
    /** 返回当前年月字符串（格式：2025-12） */
    fun currentYearMonth(): String {
        val sdf = SimpleDateFormat("yyyy-MM", Locale.getDefault())
        return sdf.format(Date())
    }

    /** 返回当前时间戳 */
    fun now(): Long = System.currentTimeMillis()
}

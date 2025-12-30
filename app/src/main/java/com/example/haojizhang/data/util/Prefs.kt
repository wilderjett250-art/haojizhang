package com.example.haojizhang.data.util

import android.content.Context

object Prefs {
    private const val SP = "haojizhang_prefs"

    private const val KEY_PIN_ENABLED = "pin_enabled"
    private const val KEY_PIN_CODE = "pin_code"

    private const val KEY_WARN_HIGH = "warn_high"     // 0.85
    private const val KEY_WARN_LOW = "warn_low"       // 0.40
    private const val KEY_TOP_SHARE = "top_share"     // 0.60

    fun isPinEnabled(ctx: Context): Boolean =
        ctx.getSharedPreferences(SP, Context.MODE_PRIVATE).getBoolean(KEY_PIN_ENABLED, false)

    fun getPin(ctx: Context): String =
        ctx.getSharedPreferences(SP, Context.MODE_PRIVATE).getString(KEY_PIN_CODE, "") ?: ""

    fun setPin(ctx: Context, pin: String) {
        ctx.getSharedPreferences(SP, Context.MODE_PRIVATE).edit()
            .putBoolean(KEY_PIN_ENABLED, pin.isNotBlank())
            .putString(KEY_PIN_CODE, pin)
            .apply()
    }

    fun setPinEnabled(ctx: Context, enabled: Boolean) {
        ctx.getSharedPreferences(SP, Context.MODE_PRIVATE).edit()
            .putBoolean(KEY_PIN_ENABLED, enabled)
            .apply()
    }

    fun getWarnHigh(ctx: Context): Float =
        ctx.getSharedPreferences(SP, Context.MODE_PRIVATE).getFloat(KEY_WARN_HIGH, 0.85f)

    fun getWarnLow(ctx: Context): Float =
        ctx.getSharedPreferences(SP, Context.MODE_PRIVATE).getFloat(KEY_WARN_LOW, 0.40f)

    fun getTopShare(ctx: Context): Float =
        ctx.getSharedPreferences(SP, Context.MODE_PRIVATE).getFloat(KEY_TOP_SHARE, 0.60f)

    fun setRules(ctx: Context, high: Float, low: Float, top: Float) {
        ctx.getSharedPreferences(SP, Context.MODE_PRIVATE).edit()
            .putFloat(KEY_WARN_HIGH, high)
            .putFloat(KEY_WARN_LOW, low)
            .putFloat(KEY_TOP_SHARE, top)
            .apply()
    }
}

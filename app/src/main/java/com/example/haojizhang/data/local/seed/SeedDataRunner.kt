package com.example.haojizhang.data.local.seed

import android.content.Context
import com.example.haojizhang.data.local.db.DbProvider

object SeedDataRunner {

    private const val SP_NAME = "haojizhang_boot"
    private const val KEY_DONE = "seed_done"

    fun ensureSeeded(context: Context) {
        val appCtx = context.applicationContext
        val sp = appCtx.getSharedPreferences(SP_NAME, Context.MODE_PRIVATE)
        if (sp.getBoolean(KEY_DONE, false)) return

        val db = DbProvider.get(appCtx)
        SeedData.ensureBlocking(db)

        sp.edit().putBoolean(KEY_DONE, true).apply()
    }
}

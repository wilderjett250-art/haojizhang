package com.example.haojizhang.data.seed

import android.content.Context
import com.example.haojizhang.data.local.db.DbProvider

object SeedDataRunner {

    suspend fun ensure(context: Context) {
        val db = DbProvider.get(context)
        val categoryDao = db.categoryDao()
        val accountDao = db.accountDao()

        if (categoryDao.countAll() == 0) {
            categoryDao.insertAll(SeedData.defaultCategories())
        }
        if (accountDao.countAll() == 0) {
            accountDao.insertAll(SeedData.defaultAccounts())
        }
    }
}

package com.example.haojizhang.data.local.seed

import com.example.haojizhang.data.local.db.HaoJizhangDatabase
import com.example.haojizhang.data.local.entity.AccountEntity
import com.example.haojizhang.data.local.entity.CategoryEntity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withContext

object SeedData {

    // type: 0 = 支出，1 = 收入
    private val defaultCategories = listOf(
        CategoryEntity(name = "餐饮", type = 0, icon = "restaurant", sortOrder = 1, isVisible = true),
        CategoryEntity(name = "交通", type = 0, icon = "directions_bus", sortOrder = 2, isVisible = true),
        CategoryEntity(name = "购物", type = 0, icon = "shopping_bag", sortOrder = 3, isVisible = true),
        CategoryEntity(name = "娱乐", type = 0, icon = "sports_esports", sortOrder = 4, isVisible = true),
        CategoryEntity(name = "居住", type = 0, icon = "home", sortOrder = 5, isVisible = true),
        CategoryEntity(name = "医疗", type = 0, icon = "medical_services", sortOrder = 6, isVisible = true),

        CategoryEntity(name = "工资", type = 1, icon = "payments", sortOrder = 1, isVisible = true),
        CategoryEntity(name = "兼职", type = 1, icon = "work", sortOrder = 2, isVisible = true),
        CategoryEntity(name = "理财", type = 1, icon = "trending_up", sortOrder = 3, isVisible = true)
    )

    private val defaultAccounts = listOf(
        AccountEntity(name = "现金", icon = "account_balance_wallet", sortOrder = 1, isActive = true),
        AccountEntity(name = "银行卡", icon = "credit_card", sortOrder = 2, isActive = true),
        AccountEntity(name = "支付宝", icon = "qr_code", sortOrder = 3, isActive = true),
        AccountEntity(name = "微信", icon = "chat", sortOrder = 4, isActive = true)
    )

    fun ensureBlocking(db: HaoJizhangDatabase) {
        runBlocking { ensure(db) }
    }

    suspend fun ensure(db: HaoJizhangDatabase) = withContext(Dispatchers.IO) {
        val cDao = db.categoryDao()
        val aDao = db.accountDao()

        if (cDao.countAll() == 0) {
            cDao.insertAll(defaultCategories)
        }
        if (aDao.countAll() == 0) {
            aDao.insertAll(defaultAccounts)
        }
    }
}

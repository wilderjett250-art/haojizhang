package com.example.haojizhang.data.seed

import com.example.haojizhang.data.local.entity.AccountEntity
import com.example.haojizhang.data.local.entity.CategoryEntity

object SeedData {

    fun defaultCategories(): List<CategoryEntity> {
        // type: 0=支出 1=收入
        return listOf(
            // 支出
            CategoryEntity(name = "餐饮", type = 0, iconKey = "food", colorInt = 0xFFE57373.toInt(), sortOrder = 1, isHidden = false),
            CategoryEntity(name = "交通", type = 0, iconKey = "car", colorInt = 0xFF64B5F6.toInt(), sortOrder = 2, isHidden = false),
            CategoryEntity(name = "购物", type = 0, iconKey = "bag", colorInt = 0xFFBA68C8.toInt(), sortOrder = 3, isHidden = false),
            CategoryEntity(name = "娱乐", type = 0, iconKey = "game", colorInt = 0xFFFFB74D.toInt(), sortOrder = 4, isHidden = false),

            // 收入
            CategoryEntity(name = "工资", type = 1, iconKey = "salary", colorInt = 0xFF81C784.toInt(), sortOrder = 1, isHidden = false),
            CategoryEntity(name = "兼职", type = 1, iconKey = "parttime", colorInt = 0xFF4DB6AC.toInt(), sortOrder = 2, isHidden = false),
            CategoryEntity(name = "红包", type = 1, iconKey = "gift", colorInt = 0xFFAED581.toInt(), sortOrder = 3, isHidden = false)
        )
    }

    fun defaultAccounts(): List<AccountEntity> {
        return listOf(
            AccountEntity(name = "现金", iconKey = "cash", colorInt = 0xFFFFCC80.toInt(), sortOrder = 1, isArchived = false),
            AccountEntity(name = "银行卡", iconKey = "card", colorInt = 0xFF90CAF9.toInt(), sortOrder = 2, isArchived = false),
            AccountEntity(name = "支付宝", iconKey = "alipay", colorInt = 0xFF80DEEA.toInt(), sortOrder = 3, isArchived = false),
            AccountEntity(name = "微信", iconKey = "wechat", colorInt = 0xFFA5D6A7.toInt(), sortOrder = 4, isArchived = false)
        )
    }
}

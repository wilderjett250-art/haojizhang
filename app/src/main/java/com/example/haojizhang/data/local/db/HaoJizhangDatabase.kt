package com.example.haojizhang.data.local.db

import androidx.room.Database
import androidx.room.RoomDatabase
import com.example.haojizhang.data.local.dao.AccountDao
import com.example.haojizhang.data.local.dao.BillDao
import com.example.haojizhang.data.local.dao.BudgetDao
import com.example.haojizhang.data.local.dao.CategoryDao
import com.example.haojizhang.data.local.entity.AccountEntity
import com.example.haojizhang.data.local.entity.BillEntity
import com.example.haojizhang.data.local.entity.BudgetEntity
import com.example.haojizhang.data.local.entity.CategoryEntity

@Database(
    entities = [
        BillEntity::class,
        CategoryEntity::class,
        AccountEntity::class,
        BudgetEntity::class
    ],
    version = 1,
    exportSchema = true
)
abstract class HaoJizhangDatabase : RoomDatabase() {
    abstract fun billDao(): BillDao
    abstract fun categoryDao(): CategoryDao
    abstract fun accountDao(): AccountDao
    abstract fun budgetDao(): BudgetDao
}

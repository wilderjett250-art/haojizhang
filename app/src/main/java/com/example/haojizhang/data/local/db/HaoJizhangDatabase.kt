package com.example.haojizhang.data.local.db

import android.content.Context
import androidx.room.Database
import androidx.room.Room
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
    entities = [BillEntity::class, CategoryEntity::class, AccountEntity::class, BudgetEntity::class],
    version = 1,
    exportSchema = false
)
abstract class HaoJizhangDatabase : RoomDatabase() {

    abstract fun billDao(): BillDao
    abstract fun categoryDao(): CategoryDao
    abstract fun accountDao(): AccountDao
    abstract fun budgetDao(): BudgetDao

    companion object {
        @Volatile private var INSTANCE: HaoJizhangDatabase? = null

        fun getInstance(context: Context): HaoJizhangDatabase {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: Room.databaseBuilder(
                    context.applicationContext,
                    HaoJizhangDatabase::class.java,
                    "haojizhang.db"
                ).build().also { INSTANCE = it }
            }
        }
    }
}

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
    entities = [
        BillEntity::class,
        CategoryEntity::class,
        AccountEntity::class,
        BudgetEntity::class
    ],
    // 由于这次把 CategoryEntity/AccountEntity 的 boolean 字段显式声明了列名，
    // 需要 bump version，防止你本地旧库字段名不一致导致各种奇怪报错。
    version = 3,
    exportSchema = true
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
                val db = Room.databaseBuilder(
                    context.applicationContext,
                    HaoJizhangDatabase::class.java,
                    "haojizhang.db"
                )
                    .fallbackToDestructiveMigration()
                    .build()
                INSTANCE = db
                db
            }
        }
    }
}

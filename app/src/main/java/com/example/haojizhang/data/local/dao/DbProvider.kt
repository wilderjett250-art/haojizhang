package com.example.haojizhang.data.local.db

import android.content.Context
import androidx.room.Room

object DbProvider {

    @Volatile
    private var instance: HaoJizhangDatabase? = null

    fun get(context: Context): HaoJizhangDatabase {
        val appCtx = context.applicationContext
        return instance ?: synchronized(this) {
            instance ?: Room.databaseBuilder(appCtx, HaoJizhangDatabase::class.java, "haojizhang.db")
                .fallbackToDestructiveMigration()
                .build()
                .also { instance = it }
        }
    }
}

package com.example.haojizhang.data.local.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "account",
    indices = [
        Index("isActive"),
        Index("sortOrder")
    ]
)
data class AccountEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0L,

    /** 账户名：现金/银行卡/支付宝/微信等 */
    val name: String,

    /** 图标（emoji 或者你自己约定的字符串） */
    val icon: String = "💳",

    /** 排序（越小越靠前） */
    val sortOrder: Int = 0,

    /** 是否启用（⚠️ 同理钉死列名） */
    @ColumnInfo(name = "isActive")
    val isActive: Boolean = true
)

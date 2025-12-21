package com.example.haojizhang.data.local.entity

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "account",
    indices = [
        Index(value = ["type", "sortOrder"])
    ]
)
data class AccountEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0L,

    /** 现金/微信/支付宝/银行卡 */
    val name: String,

    /** 账户类型（0现金 1微信 2支付宝 3银行卡 9其他） */
    val type: Int,

    /** 初始余额（分）可选 */
    val initialBalanceCent: Long = 0L,

    val note: String? = null,
    val sortOrder: Int = 0,
    val isArchived: Boolean = false
)

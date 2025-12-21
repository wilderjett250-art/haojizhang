package com.example.haojizhang.data.local.entity

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "bill",
    indices = [
        Index("occurredAt"),
        Index("type"),
        Index("categoryId"),
        Index("accountId")
    ]
)
data class BillEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0L,

    /** 金额（单位：分） */
    val amountCent: Long,

    /** 0=支出 1=收入 */
    val type: Int,

    /** 分类ID */
    val categoryId: Long,

    /** 账户ID */
    val accountId: Long,

    /** 备注（可空） */
    val note: String? = null,

    /** 发生时间（毫秒时间戳） */
    val occurredAt: Long,

    /** 创建/更新时间（毫秒时间戳） */
    val createdAt: Long,
    val updatedAt: Long
)

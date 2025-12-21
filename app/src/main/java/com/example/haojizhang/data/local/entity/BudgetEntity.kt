package com.example.haojizhang.data.local.entity

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "budget",
    indices = [Index(value = ["yearMonth"], unique = true)]
)
data class BudgetEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0L,

    /** 格式：2025-12 */
    val yearMonth: String,

    /** 预算金额（分） */
    val limitCent: Long,

    val createdAt: Long,
    val updatedAt: Long
)

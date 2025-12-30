package com.example.haojizhang.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "budget")
data class BudgetEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0L,
    val yearMonth: String,
    val limitCent: Long,
    val createdAt: Long,
    val updatedAt: Long
)

package com.example.haojizhang.data.local.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "category",
    indices = [
        Index("type"),
        Index("isVisible"),
        Index("sortOrder")
    ]
)
data class CategoryEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0L,

    /** 0=支出分类 1=收入分类 */
    val type: Int,

    /** 分类名 */
    val name: String,

    /** 图标（emoji 或者你自己约定的字符串） */
    val icon: String = "📌",

    /** 排序（越小越靠前） */
    val sortOrder: Int = 0,

    /** 是否显示（⚠️ 用 ColumnInfo 把列名钉死，避免 Room 把 isVisible 识别成 visible） */
    @ColumnInfo(name = "isVisible")
    val isVisible: Boolean = true
)

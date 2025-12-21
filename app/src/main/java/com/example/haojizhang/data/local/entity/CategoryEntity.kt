package com.example.haojizhang.data.local.entity

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "category",
    indices = [
        Index(value = ["type", "sortOrder"])
    ]
)
data class CategoryEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0L,

    /** 分类名：早餐/地铁/学习等 */
    val name: String,

    /** 0=支出分类 1=收入分类 */
    val type: Int,

    /** 图标标识（先用字符串，后面再映射成真正 icon） */
    val iconKey: String = "default",

    /** 颜色 ARGB int（例如 0xFF00AAFF.toInt()） */
    val colorInt: Int = 0xFF4CAF50.toInt(),

    /** 排序用 */
    val sortOrder: Int = 0,

    /** 是否隐藏（不删，便于恢复） */
    val isHidden: Boolean = false
)

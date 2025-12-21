package com.example.haojizhang.data.local.dao

import androidx.room.*
import com.example.haojizhang.data.local.entity.CategoryEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface CategoryDao {

    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun insert(entity: CategoryEntity): Long

    @Update
    suspend fun update(entity: CategoryEntity)

    @Query("SELECT * FROM category WHERE isHidden = 0 AND type = :type ORDER BY sortOrder ASC, id ASC")
    fun observeVisibleByType(type: Int): Flow<List<CategoryEntity>>

    @Query("SELECT * FROM category WHERE id = :id LIMIT 1")
    suspend fun getById(id: Long): CategoryEntity?
}

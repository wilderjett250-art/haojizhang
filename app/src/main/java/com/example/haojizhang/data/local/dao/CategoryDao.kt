package com.example.haojizhang.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.haojizhang.data.local.entity.CategoryEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface CategoryDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(entity: CategoryEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(list: List<CategoryEntity>): List<Long>

    @Query("SELECT COUNT(*) FROM category")
    suspend fun countAll(): Int


    @Query("SELECT * FROM category ORDER BY type ASC, sortOrder ASC, id ASC")
    suspend fun getAllForExport(): List<CategoryEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertAll(list: List<CategoryEntity>): List<Long>

    @Query("DELETE FROM category")
    suspend fun deleteAll()

    @Query(
        """
        SELECT * FROM category
        WHERE type = :type AND isVisible = 1
        ORDER BY sortOrder ASC, id ASC
        """
    )
    fun observeVisibleByType(type: Int): Flow<List<CategoryEntity>>
}

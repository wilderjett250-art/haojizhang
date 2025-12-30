package com.example.haojizhang.data.local.dao

import androidx.room.*
import com.example.haojizhang.data.local.entity.BudgetEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface BudgetDao {
    @Query("SELECT * FROM budget ORDER BY yearMonth DESC")
    suspend fun getAllForExport(): List<BudgetEntity>

    @Query("DELETE FROM budget")
    suspend fun deleteAll()

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(entity: BudgetEntity): Long

    @Query("SELECT * FROM budget WHERE yearMonth = :yearMonth LIMIT 1")
    fun observeByMonth(yearMonth: String): Flow<BudgetEntity?>

    @Query("SELECT * FROM budget WHERE yearMonth = :yearMonth LIMIT 1")
    suspend fun getByMonth(yearMonth: String): BudgetEntity?
}

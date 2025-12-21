package com.example.haojizhang.data.local.dao

import androidx.room.*
import com.example.haojizhang.data.local.entity.AccountEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface AccountDao {

    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun insert(entity: AccountEntity): Long

    @Update
    suspend fun update(entity: AccountEntity)

    @Query("SELECT * FROM account WHERE isArchived = 0 ORDER BY sortOrder ASC, id ASC")
    fun observeActive(): Flow<List<AccountEntity>>

    @Query("SELECT * FROM account WHERE id = :id LIMIT 1")
    suspend fun getById(id: Long): AccountEntity?
}

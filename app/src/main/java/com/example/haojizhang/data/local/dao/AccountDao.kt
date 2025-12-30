package com.example.haojizhang.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.haojizhang.data.local.entity.AccountEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface AccountDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(entity: AccountEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(list: List<AccountEntity>): List<Long>

    @Query("SELECT COUNT(*) FROM account")
    suspend fun countAll(): Int

    @Query(
        """
        SELECT * FROM account
        WHERE isActive = 1
        ORDER BY sortOrder ASC, id ASC
        """
    )
    fun observeActive(): Flow<List<AccountEntity>>
}

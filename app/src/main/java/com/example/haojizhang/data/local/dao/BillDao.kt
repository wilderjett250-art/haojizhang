package com.example.haojizhang.data.local.dao

import androidx.room.*
import com.example.haojizhang.data.local.entity.BillEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface BillDao {

    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun insert(entity: BillEntity): Long

    @Update
    suspend fun update(entity: BillEntity)

    @Delete
    suspend fun delete(entity: BillEntity)

    @Query("SELECT * FROM bill WHERE id = :id LIMIT 1")
    suspend fun getById(id: Long): BillEntity?

    @Query("""
        SELECT * FROM bill
        WHERE occurredAt BETWEEN :startMillis AND :endMillis
        ORDER BY occurredAt DESC
    """)
    fun observeBetween(startMillis: Long, endMillis: Long): Flow<List<BillEntity>>

    @Query("""
        SELECT * FROM bill
        WHERE (note LIKE '%' || :keyword || '%')
        ORDER BY occurredAt DESC
    """)
    fun observeSearch(keyword: String): Flow<List<BillEntity>>

    @Query("""
        SELECT COALESCE(SUM(amountCent), 0) FROM bill
        WHERE type = :type AND occurredAt BETWEEN :startMillis AND :endMillis
    """)
    fun observeSumByType(type: Int, startMillis: Long, endMillis: Long): Flow<Long>

    /** 分类汇总：返回 categoryId + sum */


    @Query("SELECT * FROM bill ORDER BY occurredAt DESC")
    suspend fun getAllForExport(): List<BillEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAllForImport(list: List<BillEntity>): List<Long>

    @Query("DELETE FROM bill")
    suspend fun deleteAll()



    @Query("""
        SELECT categoryId AS keyId, COALESCE(SUM(amountCent), 0) AS totalCent
        FROM bill
        WHERE type = :type AND occurredAt BETWEEN :startMillis AND :endMillis
        GROUP BY categoryId
        ORDER BY totalCent DESC
    """)
    fun observeCategoryAgg(type: Int, startMillis: Long, endMillis: Long): Flow<List<CategoryAggRow>>
}

data class CategoryAggRow(
    val keyId: Long,
    val totalCent: Long
)

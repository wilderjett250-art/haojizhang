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

    @Query(
        """
        SELECT * FROM bill
        WHERE occurredAt BETWEEN :startMillis AND :endMillis
        ORDER BY occurredAt DESC
        """
    )
    fun observeBetween(startMillis: Long, endMillis: Long): Flow<List<BillEntity>>

    @Query(
        """
        SELECT * FROM bill
        WHERE (note LIKE '%' || :keyword || '%')
        ORDER BY occurredAt DESC
        """
    )
    fun observeSearch(keyword: String): Flow<List<BillEntity>>

    @Query(
        """
        SELECT COALESCE(SUM(amountCent), 0) FROM bill
        WHERE type = :type AND occurredAt BETWEEN :startMillis AND :endMillis
        """
    )
    fun observeSumByType(type: Int, startMillis: Long, endMillis: Long): Flow<Long>

    /** 分类汇总：返回 categoryId + sum */
    @Query(
        """
        SELECT categoryId AS keyId, COALESCE(SUM(amountCent), 0) AS totalCent
        FROM bill
        WHERE type = :type AND occurredAt BETWEEN :startMillis AND :endMillis
        GROUP BY categoryId
        ORDER BY totalCent DESC
        """
    )
    fun observeCategoryAgg(type: Int, startMillis: Long, endMillis: Long): Flow<List<CategoryAggRow>>

    // =========================
    // ✅ 导出 CSV：一次 join 出分类名/账户名
    // =========================
    @Query(
        """
        SELECT b.id AS id,
               b.amountCent AS amountCent,
               b.type AS type,
               IFNULL(c.name, '未知分类') AS categoryName,
               IFNULL(a.name, '未知账户') AS accountName,
               b.note AS note,
               b.occurredAt AS occurredAt
        FROM bill b
        LEFT JOIN category c ON b.categoryId = c.id
        LEFT JOIN account a ON b.accountId = a.id
        ORDER BY b.occurredAt DESC
        """
    )
    suspend fun getAllForCsv(): List<BillCsvRow>
}

data class CategoryAggRow(
    val keyId: Long,
    val totalCent: Long
)

/** ✅ CSV 导出行 */
data class BillCsvRow(
    val id: Long,
    val amountCent: Long,
    val type: Int,
    val categoryName: String,
    val accountName: String,
    val note: String?,
    val occurredAt: Long
)

package com.example.haojizhang.ui.vm

import com.example.haojizhang.data.local.entity.BillEntity
import com.example.haojizhang.data.repository.BillRepository
import java.math.BigDecimal
import java.math.RoundingMode

class AddBillViewModel(
    private val repo: BillRepository
) {
    suspend fun saveBill(
        amountText: String,
        type: Int,
        categoryId: Long?,
        accountId: Long?,
        note: String?
    ): String? {
        val catId = categoryId ?: return "请选择分类"
        val accId = accountId ?: return "请选择账户"

        val amountCent = parseToCent(amountText) ?: return "金额格式不对（例如 12.5 或 12.50）"
        if (amountCent <= 0) return "金额必须大于 0"

        val now = System.currentTimeMillis()
        val entity = BillEntity(
            amountCent = amountCent,
            type = type,
            categoryId = catId,
            accountId = accId,
            note = note?.trim().takeUnless { it.isNullOrBlank() },
            occurredAt = now,
            createdAt = now,
            updatedAt = now
        )
        repo.insertBill(entity)
        return null
    }

    private fun parseToCent(text: String): Long? {
        val t = text.trim()
        if (t.isBlank()) return null
        return try {
            val bd = BigDecimal(t).setScale(2, RoundingMode.HALF_UP)
            bd.multiply(BigDecimal(100)).longValueExact()
        } catch (e: Exception) {
            null
        }
    }
}

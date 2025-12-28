package com.example.haojizhang.data.repository

import com.example.haojizhang.data.local.dao.BillDao
import com.example.haojizhang.data.model.BillType
import kotlinx.coroutines.flow.Flow

class BillRepository(
    private val billDao: BillDao
) {

    /**
     * 本月支出总额（分）
     */
    fun observeMonthExpense(
        startMillis: Long,
        endMillis: Long
    ): Flow<Long> {
        return billDao.observeSumByType(
            BillType.EXPENSE.ordinal,
            startMillis,
            endMillis
        )
    }

    /**
     * 本月收入总额（分）
     */
    fun observeMonthIncome(
        startMillis: Long,
        endMillis: Long
    ): Flow<Long> {
        return billDao.observeSumByType(
            BillType.INCOME.ordinal,
            startMillis,
            endMillis
        )
    }

    /**
     * 按分类统计支出
     */
    fun observeCategoryExpense(
        startMillis: Long,
        endMillis: Long
    ) = billDao.observeCategoryAgg(
        BillType.EXPENSE.ordinal,
        startMillis,
        endMillis
    )
}

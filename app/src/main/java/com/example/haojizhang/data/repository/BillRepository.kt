package com.example.haojizhang.data.repository

import com.example.haojizhang.data.local.dao.BillDao
import com.example.haojizhang.data.local.entity.BillEntity

class BillRepository(
    private val billDao: BillDao
) {

    suspend fun insertBill(entity: BillEntity) {
        billDao.insert(entity)
    }

    fun observeMonthExpense(startMillis: Long, endMillis: Long) =
        billDao.observeSumByType(0, startMillis, endMillis)

    fun observeMonthIncome(startMillis: Long, endMillis: Long) =
        billDao.observeSumByType(1, startMillis, endMillis)

    fun observeCategoryExpense(startMillis: Long, endMillis: Long) =
        billDao.observeCategoryAgg(0, startMillis, endMillis)
}

package com.example.haojizhang.ui.vm

import com.example.haojizhang.data.local.dao.BillDao
import kotlinx.coroutines.flow.Flow
import com.example.haojizhang.data.local.entity.BillEntity

class BillsViewModel(
    private val billDao: BillDao
) {
    fun observeBetween(startMillis: Long, endMillis: Long): Flow<List<BillEntity>> {
        return billDao.observeBetween(startMillis, endMillis)
    }
}

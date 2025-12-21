package com.example.haojizhang.data.model

enum class BillType(val code: Int) {
    EXPENSE(0),
    INCOME(1);

    companion object {
        fun from(code: Int): BillType = if (code == 1) INCOME else EXPENSE
    }
}

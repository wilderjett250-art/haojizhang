package com.example.haojizhang.data.model

enum class AccountType(val code: Int) {
    CASH(0),
    WECHAT(1),
    ALIPAY(2),
    BANK_CARD(3),
    OTHER(9);

    companion object {
        fun from(code: Int): AccountType = entries.firstOrNull { it.code == code } ?: OTHER
    }
}

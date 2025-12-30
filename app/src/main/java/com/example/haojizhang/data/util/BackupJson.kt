package com.example.haojizhang.data.util

import com.example.haojizhang.data.local.entity.*
import org.json.JSONArray
import org.json.JSONObject

data class BackupPayload(
    val categories: List<CategoryEntity>,
    val accounts: List<AccountEntity>,
    val budgets: List<BudgetEntity>,
    val bills: List<BillEntity>
)

object BackupJson {

    fun encode(p: BackupPayload): String {
        val root = JSONObject()
        root.put("version", 1)
        root.put("exportedAt", System.currentTimeMillis())

        root.put("categories", JSONArray().apply {
            p.categories.forEach { put(categoryToJson(it)) }
        })
        root.put("accounts", JSONArray().apply {
            p.accounts.forEach { put(accountToJson(it)) }
        })
        root.put("budgets", JSONArray().apply {
            p.budgets.forEach { put(budgetToJson(it)) }
        })
        root.put("bills", JSONArray().apply {
            p.bills.forEach { put(billToJson(it)) }
        })

        return root.toString(2)
    }

    fun decode(text: String): BackupPayload {
        val root = JSONObject(text)
        val categories = jsonArray(root, "categories").map { jsonToCategory(it) }
        val accounts = jsonArray(root, "accounts").map { jsonToAccount(it) }
        val budgets = jsonArray(root, "budgets").map { jsonToBudget(it) }
        val bills = jsonArray(root, "bills").map { jsonToBill(it) }
        return BackupPayload(categories, accounts, budgets, bills)
    }

    private fun jsonArray(root: JSONObject, key: String): List<JSONObject> {
        val arr = root.optJSONArray(key) ?: JSONArray()
        return (0 until arr.length()).map { arr.getJSONObject(it) }
    }

    private fun categoryToJson(e: CategoryEntity) = JSONObject().apply {
        put("id", e.id)
        put("type", e.type)
        put("name", e.name)
        put("icon", e.icon)
        put("sortOrder", e.sortOrder)
        put("isVisible", e.isVisible)
    }

    private fun accountToJson(e: AccountEntity) = JSONObject().apply {
        put("id", e.id)
        put("name", e.name)
        put("icon", e.icon)
        put("sortOrder", e.sortOrder)
        put("isActive", e.isActive)
    }

    private fun budgetToJson(e: BudgetEntity) = JSONObject().apply {
        put("id", e.id)
        put("yearMonth", e.yearMonth)
        put("limitCent", e.limitCent)
        put("createdAt", e.createdAt)
        put("updatedAt", e.updatedAt)
    }

    private fun billToJson(e: BillEntity) = JSONObject().apply {
        put("id", e.id)
        put("amountCent", e.amountCent)
        put("type", e.type)
        put("categoryId", e.categoryId)
        put("accountId", e.accountId)
        put("note", e.note)
        put("occurredAt", e.occurredAt)
        put("createdAt", e.createdAt)
        put("updatedAt", e.updatedAt)
    }

    private fun jsonToCategory(o: JSONObject) = CategoryEntity(
        id = o.optLong("id", 0L),
        type = o.getInt("type"),
        name = o.getString("name"),
        icon = o.optString("icon", "📌"),
        sortOrder = o.optInt("sortOrder", 0),
        isVisible = o.optBoolean("isVisible", true)
    )

    private fun jsonToAccount(o: JSONObject) = AccountEntity(
        id = o.optLong("id", 0L),
        name = o.getString("name"),
        icon = o.optString("icon", "💳"),
        sortOrder = o.optInt("sortOrder", 0),
        isActive = o.optBoolean("isActive", true)
    )

    private fun jsonToBudget(o: JSONObject) = BudgetEntity(
        id = o.optLong("id", 0L),
        yearMonth = o.getString("yearMonth"),
        limitCent = o.getLong("limitCent"),
        createdAt = o.getLong("createdAt"),
        updatedAt = o.getLong("updatedAt")
    )

    private fun jsonToBill(o: JSONObject) = BillEntity(
        id = o.optLong("id", 0L),
        amountCent = o.getLong("amountCent"),
        type = o.getInt("type"),
        categoryId = o.getLong("categoryId"),
        accountId = o.getLong("accountId"),
        note = o.optString("note").takeIf { it.isNotBlank() },
        occurredAt = o.getLong("occurredAt"),
        createdAt = o.getLong("createdAt"),
        updatedAt = o.getLong("updatedAt")
    )
}

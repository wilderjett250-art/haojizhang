package com.example.haojizhang.data.repository

import android.graphics.Bitmap
import android.util.Base64
import com.example.haojizhang.data.network.ApiClient
import com.example.haojizhang.data.network.BaiduOcrApi
import java.io.ByteArrayOutputStream

data class OcrParsedResult(
    val amountYuan: String?, // 自动填到金额输入框（单位：元）
    val note: String?        // 自动填到备注（店名/日期等）
)

class OcrRepository(
    private val apiKey: String,
    private val secretKey: String
) {
    private val api: BaiduOcrApi = ApiClient.baiduOcr.create(BaiduOcrApi::class.java)

    suspend fun recognizeReceipt(bitmap: Bitmap): OcrParsedResult {
        val token = api.getToken(apiKey = apiKey, secretKey = secretKey).access_token
            ?: throw IllegalStateException("获取百度 access_token 失败（请检查 API Key/Secret）")

        val base64 = bitmapToBase64Jpeg(bitmap)

        val resp = api.shoppingReceipt(accessToken = token, imageBase64 = base64)
        val wr = resp.words_result ?: emptyMap()

        // 最小可用：尽量从结构字段里捞出“总金额/实收金额/店名/日期”
        val amount = findFirstString(
            wr,
            listOf("总金额", "实收金额", "合计金额", "总价", "amount", "total")
        )
        val shop = findFirstString(
            wr,
            listOf("店名", "商户", "商家", "merchant", "shop")
        )
        val date = findFirstString(
            wr,
            listOf("消费日期", "日期", "交易日期", "date")
        )

        val note = listOfNotNull(shop, date).joinToString(" ").trim().ifBlank { null }

        // amount 有时可能带“¥”“元”，简单清洗一下
        val cleanedAmount = amount
            ?.replace("¥", "")
            ?.replace("￥", "")
            ?.replace("元", "")
            ?.trim()
            ?.ifBlank { null }

        return OcrParsedResult(
            amountYuan = cleanedAmount,
            note = note
        )
    }

    private fun bitmapToBase64Jpeg(bitmap: Bitmap): String {
        val baos = ByteArrayOutputStream()
        bitmap.compress(Bitmap.CompressFormat.JPEG, 80, baos) // 压缩一点避免过大
        return Base64.encodeToString(baos.toByteArray(), Base64.NO_WRAP)
    }

    private fun findFirstString(map: Map<String, Any>, keys: List<String>): String? {
        for (k in keys) {
            val v = map[k] ?: continue
            val s = when (v) {
                is String -> v
                is Map<*, *> -> (v["words"] as? String)
                    ?: (v["text"] as? String)
                    ?: (v["word"] as? String)
                else -> null
            }
            if (!s.isNullOrBlank()) return s.trim()
        }
        return null
    }
}

package com.example.haojizhang.ui

import org.json.JSONObject
import java.io.BufferedReader
import java.io.InputStreamReader
import java.net.HttpURLConnection
import java.net.URL
import java.net.URLEncoder
import java.nio.charset.StandardCharsets

data class FxResponse(
    val amount: Double,
    val base: String,
    val date: String,
    val rates: Map<String, Double>
)

object FrankfurterApi {

    /**
     * Frankfurter 示例：
     * https://api.frankfurter.app/latest?amount=100&from=USD&to=CNY
     */
    fun convert(amount: String, from: String, to: String): FxResponse {
        val baseUrl = "https://api.frankfurter.app/latest"
        val qAmount = URLEncoder.encode(amount, StandardCharsets.UTF_8.name())
        val qFrom = URLEncoder.encode(from, StandardCharsets.UTF_8.name())
        val qTo = URLEncoder.encode(to, StandardCharsets.UTF_8.name())
        val urlStr = "$baseUrl?amount=$qAmount&from=$qFrom&to=$qTo"

        val conn = (URL(urlStr).openConnection() as HttpURLConnection).apply {
            requestMethod = "GET"
            connectTimeout = 10_000
            readTimeout = 10_000
        }

        val code = conn.responseCode
        val stream = if (code in 200..299) conn.inputStream else conn.errorStream
        val body = BufferedReader(InputStreamReader(stream, Charsets.UTF_8)).use { it.readText() }

        if (code !in 200..299) {
            throw RuntimeException("HTTP $code: $body")
        }

        val json = JSONObject(body)
        val amountD = json.optDouble("amount", 0.0)
        val base = json.optString("base", from)
        val date = json.optString("date", "")

        val ratesObj = json.optJSONObject("rates") ?: JSONObject()
        val map = mutableMapOf<String, Double>()
        val keys = ratesObj.keys()
        while (keys.hasNext()) {
            val k = keys.next()
            map[k] = ratesObj.optDouble(k, Double.NaN)
        }

        return FxResponse(
            amount = amountD,
            base = base,
            date = date,
            rates = map.filterValues { !it.isNaN() }
        )
    }
}

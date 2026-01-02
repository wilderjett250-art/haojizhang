package com.example.haojizhang.data.network

import retrofit2.http.Field
import retrofit2.http.FormUrlEncoded
import retrofit2.http.POST
import retrofit2.http.Query

interface BaiduOcrApi {

    @POST("oauth/2.0/token")
    suspend fun getToken(
        @Query("grant_type") grantType: String = "client_credentials",
        @Query("client_id") apiKey: String,
        @Query("client_secret") secretKey: String
    ): BaiduTokenResp

    @FormUrlEncoded
    @POST("rest/2.0/ocr/v1/shopping_receipt")
    suspend fun shoppingReceipt(
        @Query("access_token") accessToken: String,
        @Field("image") imageBase64: String
    ): BaiduShoppingReceiptResp
}

data class BaiduTokenResp(
    val access_token: String?,
    val expires_in: Long?
)

/**
 * 百度返回结构字段很多，这里先用 Any 接住，够用即可
 * 后续需要更精确可以再把 words_result 映射成更细的 data class
 */
data class BaiduShoppingReceiptResp(
    val log_id: Long?,
    val words_result: Map<String, Any>?
)

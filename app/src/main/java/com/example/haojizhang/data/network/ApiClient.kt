package com.example.haojizhang.data.network

import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

object ApiClient {

    val baiduOcr: Retrofit by lazy {
        Retrofit.Builder()
            .baseUrl("https://aip.baidubce.com/")
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    }

    val fx: Retrofit by lazy {
        // Frankfurter
        Retrofit.Builder()
            .baseUrl("https://api.frankfurter.dev/")
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    }
}

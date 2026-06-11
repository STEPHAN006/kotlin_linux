package com.stephan.mobil.data.api

import com.stephan.mobil.data.model.ExchangeRateResponse
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.GET
import retrofit2.http.Path

interface ForexApiService {
    @GET("latest/{base}")
    suspend fun getRates(@Path("base") base: String = "USD"): Response<ExchangeRateResponse>
}

object ForexClient {
    private const val BASE_URL = "https://open.er-api.com/v6/"

    val api: ForexApiService = Retrofit.Builder()
        .baseUrl(BASE_URL)
        .addConverterFactory(GsonConverterFactory.create())
        .build()
        .create(ForexApiService::class.java)
}

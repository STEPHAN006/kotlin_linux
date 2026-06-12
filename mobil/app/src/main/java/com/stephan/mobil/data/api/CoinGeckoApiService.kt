package com.stephan.mobil.data.api

import com.stephan.mobil.data.model.CoinChartData
import com.stephan.mobil.data.model.CoinMarketData
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Query

interface CoinGeckoApiService {

    @GET("coins/markets")
    suspend fun getMarkets(
        @Query("vs_currency") currency: String = "usd",
        @Query("ids") ids: String,
        @Query("order") order: String = "market_cap_desc",
        @Query("sparkline") sparkline: Boolean = true,
        @Query("price_change_percentage") priceChange: String = "24h"
    ): Response<List<CoinMarketData>>

    @GET("coins/{id}/market_chart")
    suspend fun getChart(
        @Path("id") id: String,
        @Query("vs_currency") currency: String = "usd",
        @Query("days") days: String = "1"
    ): Response<CoinChartData>

    @GET("coins/{id}/ohlc")
    suspend fun getOhlc(
        @Path("id") id: String,
        @Query("vs_currency") currency: String = "usd",
        @Query("days") days: String = "7"
    ): Response<List<List<Double>>>
}

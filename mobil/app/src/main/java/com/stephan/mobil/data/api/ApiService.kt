package com.stephan.mobil.data.api

import com.stephan.mobil.data.model.BalanceResponse
import com.stephan.mobil.data.model.LoginRequest
import com.stephan.mobil.data.model.LoginResponse
import com.stephan.mobil.data.model.TransactionListResponse
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST

interface ApiService {
    
    @POST("login")
    suspend fun login(@Body request: LoginRequest): Response<LoginResponse>

    @GET("balance")
    suspend fun getBalance(): Response<BalanceResponse>

    @GET("transactions")
    suspend fun getTransactions(): Response<TransactionListResponse>
}

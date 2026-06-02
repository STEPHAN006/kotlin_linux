package com.stephan.mobil.data.api

import com.stephan.mobil.data.model.*
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Path

interface ApiService {
    @POST("login")
    suspend fun login(@Body request: LoginRequest): Response<ApiEnvelope<AuthData>>

    @POST("register")
    suspend fun register(@Body request: RegisterRequest): Response<ApiEnvelope<AuthData>>

    @GET("balance")
    suspend fun getBalance(): Response<ApiEnvelope<BalanceResponse>>

    @GET("transactions")
    suspend fun getTransactions(): Response<ApiEnvelope<List<Transaction>>>

    @GET("beneficiaries")
    suspend fun getBeneficiaries(): Response<ApiEnvelope<List<Beneficiary>>>

    @POST("beneficiaries")
    suspend fun createBeneficiary(@Body request: BeneficiaryRequest): Response<ApiEnvelope<Beneficiary>>

    @DELETE("beneficiaries/{id}")
    suspend fun deleteBeneficiary(@Path("id") id: Int): Response<ApiEnvelope<Unit>>

    @GET("cards")
    suspend fun getCards(): Response<ApiEnvelope<List<Card>>>

    @POST("cards")
    suspend fun createCard(@Body request: CreateCardRequest): Response<ApiEnvelope<Card>>

    @POST("cards/{id}/toggle")
    suspend fun toggleCard(@Path("id") id: Int): Response<ApiEnvelope<Card>>

    @POST("transfers")
    suspend fun createTransfer(@Body request: TransferRequest): Response<ApiEnvelope<TransferData>>

    @POST("transfers/verify")
    suspend fun verifyTransfer(@Body request: VerifyOtpRequest): Response<ApiEnvelope<Transfer>>

    @POST("qr/generate")
    suspend fun generateQr(@Body request: QrGenerateRequest): Response<ApiEnvelope<QrData>>

    @POST("qr/scan")
    suspend fun scanQr(@Body request: QrScanRequest): Response<ApiEnvelope<Map<String, Any>>>

    @POST("qr/pay")
    suspend fun payQr(@Body request: QrPayRequest): Response<ApiEnvelope<TransferData>>
}

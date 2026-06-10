package com.stephan.mobil.data.api

import com.stephan.mobil.data.model.*
import okhttp3.MultipartBody
import okhttp3.RequestBody
import okhttp3.ResponseBody
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.Multipart
import retrofit2.http.PATCH
import retrofit2.http.POST
import retrofit2.http.Part
import retrofit2.http.Path
import retrofit2.http.Query
import retrofit2.http.Streaming

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
    suspend fun scanQr(@Body request: QrScanRequest): Response<ApiEnvelope<QrScanResult>>

    @POST("qr/pay")
    suspend fun payQr(@Body request: QrPayRequest): Response<ApiEnvelope<TransferData>>

    // Support tickets
    @GET("support")
    suspend fun getSupportTickets(): Response<ApiEnvelope<List<SupportTicket>>>

    @POST("support")
    suspend fun createSupportTicket(@Body request: CreateTicketRequest): Response<ApiEnvelope<Map<String, Int>>>

    @GET("support/{id}")
    suspend fun getSupportTicket(@Path("id") id: Int): Response<ApiEnvelope<SupportTicketDetail>>

    @Multipart
    @POST("support/{id}/messages")
    suspend fun addSupportMessage(
        @Path("id") ticketId: Int,
        @Part("message") message: RequestBody,
        @Part attachments: List<MultipartBody.Part>
    ): Response<ApiEnvelope<Map<String, Int>>>

    @PATCH("support/{id}/close")
    suspend fun closeSupportTicket(@Path("id") id: Int): Response<ApiEnvelope<Unit>>

    @Streaming
    @GET("statements/monthly")
    suspend fun downloadStatement(@Query("account_id") accountId: Int): Response<ResponseBody>

    @Multipart
    @POST("profile/avatar")
    suspend fun uploadAvatar(@Part avatar: MultipartBody.Part): Response<ApiEnvelope<Map<String, String?>>>

    // KYC / Identity verification
    @GET("kyc/status")
    suspend fun getKycStatus(): Response<ApiEnvelope<KycStatusResponse>>

    @Multipart
    @POST("kyc/submit")
    suspend fun submitKyc(
        @Part("cin_full_name") cinFullName: RequestBody,
        @Part cinRecto: MultipartBody.Part,
        @Part cinVerso: MultipartBody.Part
    ): Response<ApiEnvelope<Map<String, String>>>
}

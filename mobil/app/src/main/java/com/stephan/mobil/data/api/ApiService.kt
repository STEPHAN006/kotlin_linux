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
import retrofit2.http.PUT
import retrofit2.http.Part
import retrofit2.http.Path
import retrofit2.http.Query
import retrofit2.http.Streaming

interface ApiService {
    @POST("login")
    suspend fun login(@Body request: LoginRequest): Response<ApiEnvelope<AuthData>>

    @POST("register")
    suspend fun register(@Body request: RegisterRequest): Response<ApiEnvelope<AuthData>>

    @POST("logout")
    suspend fun logout(): Response<ApiEnvelope<Unit>>

    @GET("user")
    suspend fun getCurrentUser(): Response<ApiEnvelope<User>>

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

    @GET("cards/{id}/reveal")
    suspend fun revealCard(@Path("id") id: Int): Response<ApiEnvelope<CardDetails>>

    @PATCH("cards/{id}/limit")
    suspend fun updateCardLimit(@Path("id") id: Int, @Body body: Map<String, Double>): Response<ApiEnvelope<Card>>

    @POST("cards/{id}/toggle")
    suspend fun toggleCard(@Path("id") id: Int): Response<ApiEnvelope<Card>>

    @DELETE("cards/{id}")
    suspend fun deleteCard(@Path("id") id: Int): Response<ApiEnvelope<Unit>>

    @POST("deposits")
    suspend fun createDeposit(@Body request: DepositRequest): Response<ApiEnvelope<DepositResult>>

    @POST("deposits/{reference}/confirm")
    suspend fun confirmDeposit(@Path("reference") reference: String): Response<ApiEnvelope<DepositResult>>

    @POST("deposits/{reference}/cancel")
    suspend fun cancelDeposit(@Path("reference") reference: String): Response<ApiEnvelope<Unit>>

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
    @GET("support/ticket")
    suspend fun getOrCreateSupportTicket(): Response<ApiEnvelope<SupportTicketDetail>>

    @Multipart
    @POST("support/ticket/{ticketId}/messages")
    suspend fun addSupportMessage(
        @Path("ticketId") ticketId: Int,
        @Part("message") message: RequestBody,
        @Part image: MultipartBody.Part?
    ): Response<ApiEnvelope<SupportTicketDetail>>

    @PATCH("support/ticket/{ticketId}/close")
    suspend fun closeSupportTicket(
        @Path("ticketId") ticketId: Int
    ): Response<ApiEnvelope<SupportTicketDetail>>

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

    // Crypto wallets & trading
    @GET("crypto/wallets")
    suspend fun getCryptoWallets(): Response<ApiEnvelope<List<CryptoWallet>>>

    @POST("crypto/buy")
    suspend fun buyCrypto(@Body request: CryptoBuyRequest): Response<ApiEnvelope<CryptoTradeResult>>

    @POST("crypto/sell")
    suspend fun sellCrypto(@Body request: CryptoSellRequest): Response<ApiEnvelope<CryptoTradeResult>>

    @POST("crypto/send")
    suspend fun sendCrypto(@Body request: CryptoSendRequest): Response<ApiEnvelope<CryptoTradeResult>>

    @POST("crypto/swap")
    suspend fun swapCrypto(@Body request: CryptoSwapRequest): Response<ApiEnvelope<CryptoSwapResult>>

    @GET("crypto/transactions")
    suspend fun getCryptoTransactions(): Response<ApiEnvelope<List<CryptoTxn>>>

    // Card payment confirmation
    @GET("card-payments/pending")
    suspend fun getPendingCardPayments(): Response<ApiEnvelope<List<PendingCardPayment>>>

    @POST("card-payments/{reference}/confirm")
    suspend fun confirmCardPayment(@Path("reference") reference: String): Response<ApiEnvelope<Unit>>

    @POST("card-payments/{reference}/decline")
    suspend fun declineCardPayment(@Path("reference") reference: String): Response<ApiEnvelope<Unit>>

    // Push notifications
    @GET("notifications")
    suspend fun getNotifications(): Response<ApiEnvelope<List<AppNotification>>>

    @POST("notifications/{id}/read")
    suspend fun markNotificationRead(@Path("id") id: Int): Response<ApiEnvelope<Unit>>

    @POST("notifications/read-all")
    suspend fun markAllNotificationsRead(): Response<ApiEnvelope<Unit>>

    @POST("fcm-token")
    suspend fun updateFcmToken(@Body body: Map<String, String>): Response<ApiEnvelope<Unit>>

    @GET("scheduled-withdrawals")
    suspend fun getScheduledWithdrawals(): Response<ApiEnvelope<List<ScheduledWithdrawal>>>

    @POST("scheduled-withdrawals")
    suspend fun createScheduledWithdrawal(@Body request: ScheduledWithdrawalRequest): Response<ApiEnvelope<ScheduledWithdrawal>>

    @PATCH("scheduled-withdrawals/{id}/toggle")
    suspend fun toggleScheduledWithdrawal(@Path("id") id: Int): Response<ApiEnvelope<ScheduledWithdrawal>>

    @DELETE("scheduled-withdrawals/{id}")
    suspend fun deleteScheduledWithdrawal(@Path("id") id: Int): Response<ApiEnvelope<Unit>>

    @PUT("profile")
    suspend fun updateProfile(@Body body: Map<String, String>): Response<ApiEnvelope<User>>
}

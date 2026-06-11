package com.stephan.mobil.data.repository

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import com.stephan.mobil.data.api.ApiService
import com.stephan.mobil.data.api.CoinGeckoClient
import com.stephan.mobil.data.api.ForexClient
import com.stephan.mobil.data.local.AppDatabase
import com.stephan.mobil.data.local.entity.toEntity
import com.stephan.mobil.data.model.*
import com.stephan.mobil.notifications.ScpayFirebaseService
import com.stephan.mobil.security.SecurityUtil
import kotlinx.coroutines.delay
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.toRequestBody
import java.io.ByteArrayOutputStream

class BankRepository(
    private val apiService: ApiService,
    private val context: Context,
    private val db: AppDatabase = AppDatabase.getInstance(context)
) {
    var useMockData = false

    private val mockAccounts = listOf(
        Account(1, "************4321", 4_850_000.0, "4 850 000,00 MGA", "MGA", "active", "checking"),
        Account(2, "************9901", 1_250_000.0, "1 250 000,00 MGA", "MGA", "active", "savings")
    )

    private val mockBalance = BalanceResponse(mockAccounts, 6_100_000.0, "6 100 000,00 MGA", "MGA")

    private val mockTransactions = listOf(
        Transaction(1, 150_000.0, "debit", "groceries", "Shoprite Ankorondrano", "TXN-DEMO-01", "2026-05-20"),
        Transaction(2, 4_500_000.0, "credit", "salary", "Salaire Mai", "TXN-DEMO-02", "2026-05-21"),
        Transaction(3, 50_000.0, "debit", "transport", "Shell carburant", "TXN-DEMO-03", "2026-05-22"),
        Transaction(4, 20_000.0, "credit", "refund", "Remboursement ami", "TXN-DEMO-04", "2026-05-25"),
        Transaction(5, 600_000.0, "debit", "transfer_out", "Virement OTP demo", "TXN-DEMO-05", "2026-05-27")
    )

    private val mockBeneficiaries = mutableListOf(
        Beneficiary(1, "Rasoa Marie", "BNI Madagascar", "************8022", "+261342222222", "bank", true),
        Beneficiary(2, "MVola Famille", "Telma MVola", "********0345", "+261341234567", "mvola", true)
    )

    private val mockCards = mutableListOf(
        Card(1, "**** **** **** 2026", "2026", "05/29", false, "virtual", 5_000_000.0)
    )

    suspend fun login(email: String, password: String): Result<User> = runCatching {
        if (useMockData) {
            delay(400)
            User(1, "Rakoto Jean", email, "+261341111111")
        } else {
            val response = apiService.login(LoginRequest(email, password))
            val body = response.body()
            if (!response.isSuccessful || body == null) {
                val errMsg = response.errorBody()?.string()
                    ?.let { runCatching { org.json.JSONObject(it).optString("message") }.getOrNull() }
                    ?.takeIf { it.isNotBlank() }
                    ?: "Email ou mot de passe incorrect"
                error(errMsg)
            }
            SecurityUtil.saveAuthToken(context, body.data.token)
            syncFcmToken()
            body.data.user
        }
    }

    private suspend fun syncFcmToken() {
        val token = context.getSharedPreferences(ScpayFirebaseService.PREFS, Context.MODE_PRIVATE)
            .getString(ScpayFirebaseService.KEY_TOKEN, null) ?: return
        runCatching { apiService.updateFcmToken(mapOf("token" to token)) }
    }

    suspend fun register(name: String, email: String, phone: String, password: String): Result<User> = runCatching {
        val request = RegisterRequest(name, email, phone, password, password)
        if (useMockData) {
            delay(400)
            User(10, name, email, phone)
        } else {
            val response = apiService.register(request)
            val body = response.body()
            require(response.isSuccessful && body != null) { "Inscription refusee: ${response.code()}" }
            SecurityUtil.saveAuthToken(context, body.data.token)
            body.data.user
        }
    }

    suspend fun getCurrentUser(): Result<User> = runCatching {
        val response = apiService.getCurrentUser()
        val body = response.body()
        require(response.isSuccessful && body != null) { "Session expirée" }
        body.data
    }

    suspend fun getBalance(): Result<BalanceResponse> = runCatching {
        if (useMockData) { delay(250); return@runCatching mockBalance }
        try {
            val response = apiService.getBalance()
            val body = response.body()
            require(response.isSuccessful && body != null) { "Erreur balance: ${response.code()}" }
            db.accountDao().deleteAll()
            db.accountDao().insertAll(body.data.accounts.map { it.toEntity() })
            body.data
        } catch (e: Exception) {
            val cached = db.accountDao().getAll()
            if (cached.isNotEmpty()) {
                val accounts = cached.map { it.toModel() }
                val total = accounts.sumOf { it.balance }
                BalanceResponse(accounts, total, String.format("%,.0f MGA (cache)", total), "MGA")
            } else throw e
        }
    }

    suspend fun getTransactions(): Result<List<Transaction>> = runCatching {
        if (useMockData) { delay(250); return@runCatching mockTransactions }
        try {
            val response = apiService.getTransactions()
            val body = response.body()
            require(response.isSuccessful && body != null) { "Erreur transactions: ${response.code()}" }
            db.transactionDao().deleteAll()
            db.transactionDao().insertAll(body.data.map { it.toEntity() })
            body.data
        } catch (e: Exception) {
            val cached = db.transactionDao().getAll()
            if (cached.isNotEmpty()) cached.map { it.toModel() } else throw e
        }
    }

    suspend fun getBeneficiaries(): Result<List<Beneficiary>> = runCatching {
        if (useMockData) mockBeneficiaries else apiService.getBeneficiaries().bodyOrThrow()
    }

    suspend fun addBeneficiary(request: BeneficiaryRequest): Result<Beneficiary> = runCatching {
        if (useMockData) {
            val item = Beneficiary(mockBeneficiaries.size + 1, request.name, request.bankName, "********" + request.accountNumber.takeLast(4), request.phone, request.channel, true)
            mockBeneficiaries.add(item)
            item
        } else apiService.createBeneficiary(request).bodyOrThrow()
    }

    suspend fun deleteBeneficiary(id: Int): Result<Unit> = runCatching {
        if (useMockData) {
            mockBeneficiaries.removeAll { it.id == id }
            Unit
        } else apiService.deleteBeneficiary(id).bodyOrThrow()
    }

    suspend fun getCards(): Result<List<Card>> = runCatching {
        if (useMockData) mockCards else apiService.getCards().bodyOrThrow()
    }

    suspend fun createCard(accountId: Int, limit: Double): Result<Card> = runCatching {
        if (useMockData) {
            val card = Card(mockCards.size + 1, "**** **** **** ${1000 + mockCards.size}", "${1000 + mockCards.size}", "05/29", false, "virtual", limit)
            mockCards.add(card)
            card
        } else apiService.createCard(CreateCardRequest(accountId, limit)).bodyOrThrow()
    }

    suspend fun toggleCard(cardId: Int): Result<Card> = runCatching {
        if (useMockData) {
            val index = mockCards.indexOfFirst { it.id == cardId }
            val updated = mockCards[index].copy(isBlocked = !mockCards[index].isBlocked)
            mockCards[index] = updated
            updated
        } else apiService.toggleCard(cardId).bodyOrThrow()
    }

    suspend fun updateCardLimit(cardId: Int, limit: Double): Result<Card> = runCatching {
        if (useMockData) {
            val index = mockCards.indexOfFirst { it.id == cardId }
            val updated = mockCards[index].copy(dailyLimit = limit)
            mockCards[index] = updated
            updated
        } else apiService.updateCardLimit(cardId, mapOf("daily_limit" to limit)).bodyOrThrow()
    }

    suspend fun deleteCard(cardId: Int): Result<Unit> = runCatching {
        if (useMockData) {
            mockCards.removeIf { it.id == cardId }
        } else {
            apiService.deleteCard(cardId).bodyOrThrow()
        }
    }

    suspend fun revealCard(cardId: Int): Result<CardDetails> = runCatching {
        if (useMockData) CardDetails("4539 1488 0343 6467", "742", "05/29")
        else apiService.revealCard(cardId).bodyOrThrow()
    }

    suspend fun deposit(request: DepositRequest): Result<DepositResult> = runCatching {
        if (useMockData) {
            delay(350)
            DepositResult("DEP-DEMO-0001", request.amount, request.method, "pending")
        } else apiService.createDeposit(request).bodyOrThrow()
    }

    suspend fun confirmDeposit(reference: String): Result<DepositResult> = runCatching {
        if (useMockData) {
            delay(500)
            DepositResult("DEP-DEMO-0001", 50_000.0, "mvola", "completed", 150_000.0)
        } else apiService.confirmDeposit(reference).bodyOrThrow()
    }

    suspend fun cancelDeposit(reference: String): Result<Unit> = runCatching {
        if (useMockData) { delay(200) }
        else apiService.cancelDeposit(reference).bodyOrThrow()
    }

    suspend fun transfer(request: TransferRequest): Result<TransferData> = runCatching {
        if (useMockData) {
            delay(350)
            val otp = request.amount >= 500_000.0
            TransferData(otp, Transfer(99, request.amount, if (otp) "pending" else "completed", "TRF-DEMO-OTP", !otp, request.note, request.channel))
        } else apiService.createTransfer(request).bodyOrThrow()
    }

    suspend fun verifyOtp(reference: String, otp: String): Result<Transfer> = runCatching {
        if (useMockData) Transfer(99, 600_000.0, "completed", reference, true, "Demo OTP", "internal")
        else apiService.verifyTransfer(VerifyOtpRequest(reference, otp)).bodyOrThrow()
    }

    suspend fun generateQr(accountId: Int, amount: Double?): Result<QrData> = runCatching {
        if (useMockData) QrData("eyJ0eXBlIjoiYmFua19wYXltZW50IiwiZGVtbyI6dHJ1ZX0=", mapOf("account_id" to accountId, "amount" to (amount ?: 0.0)))
        else apiService.generateQr(QrGenerateRequest(accountId, amount)).bodyOrThrow()
    }

    suspend fun scanQr(payload: String): Result<QrScanResult> = runCatching {
        val cleanedPayload = payload.trim()
        if (useMockData) {
            QrScanResult(
                payload        = cleanedPayload,
                recipientName  = "Rasoa Marie (demo)",
                accountMasked  = "•••• 8022",
                suggestedAmount = null,
                currency       = "MGA"
            )
        } else {
            apiService.scanQr(QrScanRequest(cleanedPayload)).bodyOrThrow()
        }
    }

    suspend fun payQr(senderAccountId: Int, payload: String, amount: Double): Result<TransferData> = runCatching {
        if (useMockData) {
            delay(350)
            val otp = amount >= 500_000.0
            TransferData(otp, Transfer(100, amount, if (otp) "pending" else "completed", "QR-DEMO-PAY", !otp, "Paiement QR SCpay", "internal"))
        } else apiService.payQr(QrPayRequest(senderAccountId, payload, amount)).bodyOrThrow()
    }

    private fun uriToJpegPart(uri: Uri, fieldName: String): MultipartBody.Part {
        val stream = context.contentResolver.openInputStream(uri)
            ?: error("Impossible d'ouvrir l'image")
        val bitmap = BitmapFactory.decodeStream(stream)
        stream.close()
        require(bitmap != null) { "Image illisible ou format non supporté" }
        val out = ByteArrayOutputStream()
        bitmap.compress(Bitmap.CompressFormat.JPEG, 90, out)
        val bytes = out.toByteArray()
        val body = bytes.toRequestBody("image/jpeg".toMediaTypeOrNull())
        return MultipartBody.Part.createFormData(fieldName, "$fieldName.jpg", body)
    }

    suspend fun uploadAvatar(uri: Uri): Result<String?> = runCatching {
        val part = uriToJpegPart(uri, "avatar")
        val response = apiService.uploadAvatar(part)
        require(response.isSuccessful && response.body() != null) { "Erreur upload: ${response.code()}" }
        response.body()!!.data["avatar_url"]
    }

    suspend fun downloadStatement(accountId: Int): Result<ByteArray> = runCatching {
        if (useMockData) {
            "%PDF-1.4 Mock statement for demo mode".toByteArray()
        } else {
            val response = apiService.downloadStatement(accountId)
            require(response.isSuccessful && response.body() != null) { "Erreur téléchargement PDF: ${response.code()}" }
            response.body()!!.bytes()
        }
    }

    // ── Crypto ──────────────────────────────────────────────────────────────

    suspend fun getCryptoWallets(): Result<List<CryptoWallet>> = runCatching {
        if (useMockData) emptyList()
        else apiService.getCryptoWallets().bodyOrThrow()
    }

    suspend fun getCryptoTransactions(): Result<List<CryptoTxn>> = runCatching {
        if (useMockData) emptyList()
        else apiService.getCryptoTransactions().bodyOrThrow()
    }

    suspend fun buyCrypto(symbol: String, amountMga: Double, priceUsd: Double, mgaPerUsd: Double): Result<CryptoTradeResult> = runCatching {
        if (useMockData) CryptoTradeResult(symbol, amountMga / (priceUsd * mgaPerUsd), amountMga)
        else apiService.buyCrypto(CryptoBuyRequest(symbol, amountMga, priceUsd, mgaPerUsd)).bodyOrThrow()
    }

    suspend fun sellCrypto(symbol: String, cryptoAmount: Double, priceUsd: Double, mgaPerUsd: Double): Result<CryptoTradeResult> = runCatching {
        if (useMockData) CryptoTradeResult(symbol, cryptoAmount, cryptoAmount * priceUsd * mgaPerUsd)
        else apiService.sellCrypto(CryptoSellRequest(symbol, cryptoAmount, priceUsd, mgaPerUsd)).bodyOrThrow()
    }

    suspend fun sendCrypto(symbol: String, cryptoAmount: Double, toAddress: String, priceUsd: Double, mgaPerUsd: Double): Result<CryptoTradeResult> = runCatching {
        if (useMockData) CryptoTradeResult(symbol, cryptoAmount, null, "0xmock_hash")
        else apiService.sendCrypto(CryptoSendRequest(symbol, cryptoAmount, toAddress, priceUsd, mgaPerUsd)).bodyOrThrow()
    }

    suspend fun swapCrypto(fromSymbol: String, fromAmount: Double, fromPriceUsd: Double, toSymbol: String, toPriceUsd: Double, mgaPerUsd: Double): Result<CryptoSwapResult> = runCatching {
        if (useMockData) CryptoSwapResult(fromSymbol, toSymbol, fromAmount, fromAmount * fromPriceUsd / toPriceUsd, "0xmock_swap")
        else apiService.swapCrypto(CryptoSwapRequest(fromSymbol, toSymbol, fromAmount, fromPriceUsd, toPriceUsd, mgaPerUsd)).bodyOrThrow()
    }

    suspend fun getCoinMarkets(): Result<List<CoinMarketData>> = runCatching {
        val response = CoinGeckoClient.api.getMarkets(ids = CoinGeckoClient.COIN_IDS)
        require(response.isSuccessful && response.body() != null) { "CoinGecko error: ${response.code()}" }
        response.body()!!
    }

    suspend fun getCoinChart(coinId: String, days: String): Result<List<Pair<Long, Double>>> = runCatching {
        val response = CoinGeckoClient.api.getChart(coinId, days = days)
        require(response.isSuccessful && response.body() != null) { "CoinGecko chart error: ${response.code()}" }
        response.body()!!.prices.map { row -> row[0].toLong() to row[1] }
    }

    suspend fun getExchangeRates(): Result<Map<String, Double>> = runCatching {
        val response = ForexClient.api.getRates("USD")
        require(response.isSuccessful && response.body() != null) { "Forex error: ${response.code()}" }
        response.body()!!.rates
    }

    suspend fun getCoinOhlc(coinId: String, days: String): Result<List<List<Double>>> = runCatching {
        val response = CoinGeckoClient.api.getOhlc(coinId, days = days)
        require(response.isSuccessful && response.body() != null) { "CoinGecko OHLC error: ${response.code()}" }
        response.body()!!
    }

    // ── Notifications ────────────────────────────────────────────────────────

    suspend fun getPendingCardPayments(): Result<List<PendingCardPayment>> = runCatching {
        if (useMockData) emptyList()
        else apiService.getPendingCardPayments().bodyOrThrow()
    }

    suspend fun confirmCardPayment(reference: String): Result<Unit> = runCatching {
        if (!useMockData) apiService.confirmCardPayment(reference).bodyOrThrow()
    }

    suspend fun declineCardPayment(reference: String): Result<Unit> = runCatching {
        if (!useMockData) apiService.declineCardPayment(reference).bodyOrThrow()
    }

    suspend fun getNotifications(): Result<List<AppNotification>> = runCatching {
        if (useMockData) return@runCatching emptyList()
        try {
            val list = apiService.getNotifications().bodyOrThrow()
            db.notificationDao().insertAll(list.map { it.toEntity() })
            list
        } catch (e: Exception) {
            val cached = db.notificationDao().getAll()
            if (cached.isNotEmpty()) cached.map { it.toModel() } else throw e
        }
    }

    suspend fun markNotificationRead(id: Int): Result<Unit> = runCatching {
        if (!useMockData) {
            apiService.markNotificationRead(id).bodyOrThrow()
            db.notificationDao().markRead(id)
        }
    }

    suspend fun markAllNotificationsRead(): Result<Unit> = runCatching {
        if (!useMockData) {
            apiService.markAllNotificationsRead().bodyOrThrow()
            db.notificationDao().markAllRead()
        }
    }

    // ── KYC ─────────────────────────────────────────────────────────────────

    suspend fun getKycStatus(): Result<KycStatusResponse> = runCatching {
        if (useMockData) KycStatusResponse("none")
        else apiService.getKycStatus().bodyOrThrow()
    }

    suspend fun submitKyc(cinFullName: String, cinRectoUri: Uri, cinVersoUri: Uri): Result<Unit> = runCatching {
        if (useMockData) {
            delay(800)
            return@runCatching
        }
        val nameBody = cinFullName.toRequestBody("text/plain".toMediaTypeOrNull())
        val response = apiService.submitKyc(
            nameBody,
            uriToJpegPart(cinRectoUri, "cin_recto"),
            uriToJpegPart(cinVersoUri, "cin_verso")
        )
        require(response.isSuccessful && response.body() != null) { "Erreur soumission KYC: ${response.code()}" }
    }

    suspend fun getOrCreateSupportTicket(): Result<SupportTicketDetail> = runCatching {
        if (useMockData) return@runCatching SupportTicketDetail(
            id = 1, subject = "Support client", status = "open", priority = "medium", category = "general",
            messages = listOf(
                SupportMessage(1, "Bienvenue chez SCpay ! Décrivez votre problème.", true, "Agent SCpay")
            )
        )
        apiService.getOrCreateSupportTicket().bodyOrThrow()
    }

    suspend fun sendSupportMessage(
        ticketId: Int,
        message: String,
        imageUri: Uri? = null,
        ctx: Context? = null
    ): Result<SupportTicketDetail> = runCatching {
        if (useMockData) {
            delay(300)
            return@runCatching SupportTicketDetail(ticketId, "Support client", "open", "medium", "general")
        }
        val msgBody = message.toRequestBody("text/plain".toMediaTypeOrNull())
        val imagePart = if (imageUri != null) uriToJpegPart(imageUri, "image") else null
        apiService.addSupportMessage(ticketId, msgBody, imagePart).bodyOrThrow()
    }

    suspend fun updateProfile(
        currentPassword: String,
        name: String? = null,
        email: String? = null,
        phone: String? = null
    ): Result<User> = runCatching {
        if (useMockData) error("Non disponible en mode mock")
        apiService.updateProfile(mapOf(
            "current_password" to currentPassword,
            "name" to name,
            "email" to email,
            "phone" to phone
        ).filterValues { it != null }.mapValues { it.value!! }).bodyOrThrow()
    }

    suspend fun getScheduledWithdrawals(): Result<List<ScheduledWithdrawal>> = runCatching {
        if (useMockData) emptyList()
        else apiService.getScheduledWithdrawals().bodyOrThrow()
    }

    suspend fun createScheduledWithdrawal(request: ScheduledWithdrawalRequest): Result<ScheduledWithdrawal> = runCatching {
        if (useMockData) error("Non disponible en mode mock")
        else apiService.createScheduledWithdrawal(request).bodyOrThrow()
    }

    suspend fun toggleScheduledWithdrawal(id: Int): Result<ScheduledWithdrawal> = runCatching {
        if (useMockData) error("Non disponible en mode mock")
        else apiService.toggleScheduledWithdrawal(id).bodyOrThrow()
    }

    suspend fun deleteScheduledWithdrawal(id: Int): Result<Unit> = runCatching {
        if (useMockData) return@runCatching
        apiService.deleteScheduledWithdrawal(id).bodyOrThrow()
    }

    fun logout() {
        SecurityUtil.clearData(context)
        useMockData = false
    }

    private fun <T> retrofit2.Response<ApiEnvelope<T>>.bodyOrThrow(): T {
        val body = body()
        require(isSuccessful && body != null) { "Erreur API: ${code()}" }
        return body.data
    }
}

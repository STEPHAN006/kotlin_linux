package com.stephan.mobil.data.repository

import android.content.Context
import com.stephan.mobil.data.api.ApiService
import com.stephan.mobil.data.model.*
import com.stephan.mobil.security.SecurityUtil
import kotlinx.coroutines.delay

class BankRepository(
    private val apiService: ApiService,
    private val context: Context
) {
    val appContext: Context get() = context.applicationContext
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

    /** Restore session from saved token — called on every app start. */
    suspend fun restoreSession(): Result<User> = runCatching {
        if (useMockData) {
            delay(200)
            User(1, "Rakoto Jean", "rakoto@example.com", "+261341111111")
        } else {
            require(!SecurityUtil.getAuthToken(context).isNullOrBlank()) { "No saved token" }
            val response = apiService.getUser()
            val body = response.body()
            require(response.isSuccessful && body != null) { "Session expirée" }
            body.data
        }
    }

    suspend fun logout(): Result<Unit> = runCatching {
        if (!useMockData) apiService.logout()
    }

    suspend fun login(email: String, password: String): Result<User> = runCatching {
        if (useMockData) {
            delay(400)
            User(1, "Rakoto Jean", email, "+261341111111")
        } else {
            val response = apiService.login(LoginRequest(email, password))
            val body = response.body()
            require(response.isSuccessful && body != null) { "Login refuse: ${response.code()}" }
            SecurityUtil.saveAuthToken(context, body.data.token)
            body.data.user
        }
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

    suspend fun getBalance(): Result<BalanceResponse> = runCatching {
        if (useMockData) {
            delay(250)
            mockBalance
        } else {
            val response = apiService.getBalance()
            val body = response.body()
            require(response.isSuccessful && body != null) { "Erreur balance: ${response.code()}" }
            body.data
        }
    }

    suspend fun getTransactions(): Result<List<Transaction>> = runCatching {
        if (useMockData) {
            delay(250)
            mockTransactions
        } else {
            val response = apiService.getTransactions()
            val body = response.body()
            require(response.isSuccessful && body != null) { "Erreur transactions: ${response.code()}" }
            body.data
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
        if (useMockData) {
            val payload = mapOf(
                "type" to "bank_payment",
                "account_id" to accountId,
                "amount" to (amount ?: 0.0),
                "currency" to "MGA",
                "nonce" to "demo"
            )
            val json = com.google.gson.Gson().toJson(payload)
            val encoded = android.util.Base64.encodeToString(json.toByteArray(Charsets.UTF_8), android.util.Base64.NO_WRAP)
            QrData(encoded, payload)
        } else apiService.generateQr(QrGenerateRequest(accountId, amount)).bodyOrThrow()
    }

    suspend fun scanQr(payload: String): Result<Map<String, Any>> = runCatching {
        // Clean payload – remove extra whitespace and line breaks
        val cleanedPayload = payload.trim()
        // Log payload for debugging; remove in production
        android.util.Log.d("BankRepository", "Scanning QR payload: $cleanedPayload")
        if (useMockData) {
            mapOf("type" to "bank_payment", "payload" to cleanedPayload, "message" to "QR code valide en mode demo")
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

    private val mockNotifications = listOf(
        AppNotification(1, "Veuillez approuver votre paiement 5 USD", "Veuillez approuver votre paiement.\n05-15 20:58", false, "2026-05-15 20:58"),
        AppNotification(2, "Votre transaction 20,00 USD a été refusée", "Carte 1000 — solde insuffisant.\n04-15 11:56", true, "2026-04-15 11:56"),
        AppNotification(3, "Carte activée. Vous êtes prêt", "Votre voyage de paiement sécurisé commence maintenant.\n03-07 09:32", true, "2026-03-07 09:32")
    )

    suspend fun getNotifications(): Result<List<AppNotification>> = runCatching {
        if (useMockData) mockNotifications
        else apiService.getNotifications().bodyOrThrow()
    }

    suspend fun markAllNotificationsRead(): Result<Unit> = runCatching {
        if (!useMockData) apiService.markAllNotificationsRead().bodyOrThrow()
    }

    suspend fun getSupportTicket(): Result<SupportTicket> = runCatching {
        if (useMockData) {
            SupportTicket(
                id = 1, subject = "Support client", status = "open",
                messages = listOf(
                    SupportMessage(1, "admin", "Bienvenue chez SCpay ! Je suis votre assistant SCpay, ici pour vous aider.\n\nPour que je puisse vous offrir la meilleure solution, veuillez décrire votre question avec le plus de détails possible.", "2026-06-04 09:00")
                )
            )
        } else apiService.getSupportTicket().bodyOrThrow()
    }

    suspend fun sendSupportMessage(ticketId: Int, message: String): Result<SupportTicket> = runCatching {
        if (useMockData) {
            SupportTicket(
                id = ticketId, subject = "Support client", status = "open",
                messages = listOf(
                    SupportMessage(1, "admin", "Bienvenue chez SCpay ! Je suis votre assistant SCpay, ici pour vous aider.", "2026-06-04 09:00"),
                    SupportMessage(2, "user", message, "2026-06-04 09:01"),
                    SupportMessage(3, "admin", "Merci, un agent SCpay va traiter votre demande. Vous pouvez aussi consulter le Centre d'aide.", "2026-06-04 09:01")
                )
            )
        } else apiService.sendSupportMessage(ticketId, SendMessageRequest(message)).bodyOrThrow()
    }

    private fun <T> retrofit2.Response<ApiEnvelope<T>>.bodyOrThrow(): T {
        val body = body()
        require(isSuccessful && body != null) { "Erreur API: ${code()}" }
        return body.data
    }
}

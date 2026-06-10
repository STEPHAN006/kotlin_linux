package com.stephan.mobil.ui.viewmodel

import android.content.Context
import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.stephan.mobil.data.model.*
import com.stephan.mobil.data.repository.BankRepository
import com.stephan.mobil.ui.notifications.NotificationHelper
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class BankUiState(
    val loading: Boolean = true,
    val user: User? = null,
    val balance: BalanceResponse = BalanceResponse(),
    val transactions: List<Transaction> = emptyList(),
    val beneficiaries: List<Beneficiary> = emptyList(),
    val cards: List<Card> = emptyList(),
    val pendingTransfer: Transfer? = null,
    val qrPayload: String? = null,
    val qrScanResult: QrScanResult? = null,
    val message: String? = null,
    val error: String? = null,
    val mockMode: Boolean = true,
    val kycRejectionReason: String? = null,
    val kycSubmitting: Boolean = false,
    val cryptoWallets: List<CryptoWallet> = emptyList(),
    val cryptoMarkets: List<CoinMarketData> = emptyList(),
    val cryptoChart: List<Pair<Long, Double>> = emptyList(),
    val cryptoLoading: Boolean = false,
    val mgaPerUsd: Double = 4500.0
)

class BankViewModel(private val repository: BankRepository, private val appContext: Context? = null) : ViewModel() {
    private val _uiState = MutableStateFlow(BankUiState(mockMode = repository.useMockData))
    val uiState: StateFlow<BankUiState> = _uiState.asStateFlow()

    init {
        refreshAll()
    }

    fun setMockMode(enabled: Boolean) {
        repository.useMockData = enabled
        _uiState.value = _uiState.value.copy(mockMode = enabled, message = if (enabled) "Mode mock active" else "Mode API active")
        refreshAll()
    }

    fun login(email: String, password: String) = viewModelScope.launch {
        repository.login(email, password).fold(
            onSuccess = { user ->
                val kycInfo = if (!repository.useMockData) repository.getKycStatus().getOrNull() else null
                _uiState.value = _uiState.value.copy(
                    user = if (kycInfo != null) user.copy(kycStatus = kycInfo.status) else user,
                    kycRejectionReason = kycInfo?.rejectionReason,
                    message = "Connecte"
                )
            },
            onFailure = { _uiState.value = _uiState.value.copy(error = it.message) }
        )
        refreshAll()
    }

    fun register(name: String, email: String, phone: String, password: String) = viewModelScope.launch {
        repository.register(name, email, phone, password).fold(
            onSuccess = { _uiState.value = _uiState.value.copy(user = it, message = "Compte cree") },
            onFailure = { _uiState.value = _uiState.value.copy(error = it.message) }
        )
        refreshAll()
    }

    fun refreshAll() = viewModelScope.launch {
        _uiState.value = _uiState.value.copy(loading = true, error = null)
        val balance = repository.getBalance()
        val transactions = repository.getTransactions()
        val beneficiaries = repository.getBeneficiaries()
        val cards = repository.getCards()

        _uiState.value = _uiState.value.copy(
            loading = false,
            balance = balance.getOrDefault(BalanceResponse()),
            transactions = transactions.getOrDefault(emptyList()),
            beneficiaries = beneficiaries.getOrDefault(emptyList()),
            cards = cards.getOrDefault(emptyList()),
            error = listOf(balance, transactions, beneficiaries, cards).firstOrNull { it.isFailure }?.exceptionOrNull()?.message
        )
    }

    fun addBeneficiary(name: String, accountNumber: String, phone: String, channel: String) = viewModelScope.launch {
        val bank = when (channel) {
            "mvola" -> "Telma MVola"
            "airtel_money" -> "Airtel Money"
            "orange_money" -> "Orange Money"
            else -> "BNI Madagascar"
        }
        repository.addBeneficiary(BeneficiaryRequest(name, bank, accountNumber, phone.ifBlank { null }, channel)).fold(
            onSuccess = { refreshAll() },
            onFailure = { _uiState.value = _uiState.value.copy(error = it.message) }
        )
    }

    fun deleteBeneficiary(id: Int) = viewModelScope.launch {
        repository.deleteBeneficiary(id).fold(
            onSuccess = {
                _uiState.value = _uiState.value.copy(message = "Beneficiaire supprime")
                refreshAll()
            },
            onFailure = { _uiState.value = _uiState.value.copy(error = it.message) }
        )
    }

    fun createTransfer(senderId: Int, receiverId: Int, amount: Double, note: String) = viewModelScope.launch {
        repository.transfer(TransferRequest(senderId, receiverId, amount, note.ifBlank { null })).fold(
            onSuccess = {
                val amountStr = String.format("%,.0f", amount)
                appContext?.let { ctx ->
                    NotificationHelper.notifyTransfer(ctx, amountStr, it.transfer.status)
                }
                _uiState.value = _uiState.value.copy(
                    pendingTransfer = if (it.otpRequired) it.transfer else null,
                    message = if (it.otpRequired) "OTP requis: utilise 123456 en mock, ou lis laravel.log en API" else "Virement execute"
                )
                refreshAll()
            },
            onFailure = { _uiState.value = _uiState.value.copy(error = it.message) }
        )
    }

    fun verifyOtp(otp: String) = viewModelScope.launch {
        val reference = _uiState.value.pendingTransfer?.reference ?: return@launch
        val amount = _uiState.value.pendingTransfer?.amount ?: 0.0
        repository.verifyOtp(reference, otp).fold(
            onSuccess = {
                val amountStr = String.format("%,.0f", amount)
                appContext?.let { ctx ->
                    NotificationHelper.notifyTransfer(ctx, amountStr, "completed")
                }
                _uiState.value = _uiState.value.copy(pendingTransfer = null, message = "OTP valide")
                refreshAll()
            },
            onFailure = { _uiState.value = _uiState.value.copy(error = it.message) }
        )
    }

    fun createCard(accountId: Int, limit: Double) = viewModelScope.launch {
        repository.createCard(accountId, limit).fold(
            onSuccess = { refreshAll() },
            onFailure = { _uiState.value = _uiState.value.copy(error = it.message) }
        )
    }

    fun toggleCard(cardId: Int) = viewModelScope.launch {
        repository.toggleCard(cardId).fold(
            onSuccess = { refreshAll() },
            onFailure = { _uiState.value = _uiState.value.copy(error = it.message) }
        )
    }

    fun notify(message: String) {
        _uiState.value = _uiState.value.copy(message = message)
    }

    fun generateQr(accountId: Int, amount: Double?) = viewModelScope.launch {
        repository.generateQr(accountId, amount).fold(
            onSuccess = { _uiState.value = _uiState.value.copy(qrPayload = it.payload, message = "QR genere") },
            onFailure = { _uiState.value = _uiState.value.copy(error = it.message) }
        )
    }

    fun scanQr(payload: String) = viewModelScope.launch {
        if (payload.isBlank()) {
            _uiState.value = _uiState.value.copy(error = "QR payload vide ou invalide")
            return@launch
        }
        _uiState.value = _uiState.value.copy(loading = true)
        repository.scanQr(payload).fold(
            onSuccess = { result ->
                _uiState.value = _uiState.value.copy(loading = false, qrScanResult = result)
            },
            onFailure = { _uiState.value = _uiState.value.copy(loading = false, error = "QR invalide: ${it.message}") }
        )
    }

    fun clearQrScan() {
        _uiState.value = _uiState.value.copy(qrScanResult = null)
    }

    fun payQr(senderAccountId: Int, payload: String, amount: Double) = viewModelScope.launch {
        repository.payQr(senderAccountId, payload, amount).fold(
            onSuccess = {
                val amountStr = String.format("%,.0f", amount)
                appContext?.let { ctx ->
                    NotificationHelper.notifyQrPayment(ctx, amountStr)
                }
                _uiState.value = _uiState.value.copy(
                    pendingTransfer = if (it.otpRequired) it.transfer else null,
                    message = if (it.otpRequired) "OTP requis pour finaliser le paiement QR" else "Paiement QR envoye"
                )
                refreshAll()
            },
            onFailure = { _uiState.value = _uiState.value.copy(error = it.message) }
        )
    }

    fun uploadAvatar(uri: Uri) = viewModelScope.launch {
        repository.uploadAvatar(uri).fold(
            onSuccess = { avatarUrl ->
                val updatedUser = _uiState.value.user?.copy(avatarUrl = avatarUrl)
                _uiState.value = _uiState.value.copy(user = updatedUser, message = "Photo de profil mise à jour")
            },
            onFailure = { _uiState.value = _uiState.value.copy(error = "Échec upload: ${it.message}") }
        )
    }

    fun downloadStatement(context: Context, accountId: Int) = viewModelScope.launch {
        repository.downloadStatement(accountId).fold(
            onSuccess = { bytes ->
                try {
                    val fileName = "releve_scpay_${System.currentTimeMillis()}.pdf"
                    val file = java.io.File(
                        context.getExternalFilesDir(android.os.Environment.DIRECTORY_DOWNLOADS),
                        fileName
                    )
                    file.writeBytes(bytes)
                    val uri = androidx.core.content.FileProvider.getUriForFile(
                        context,
                        "${context.packageName}.provider",
                        file
                    )
                    val intent = android.content.Intent(android.content.Intent.ACTION_VIEW).apply {
                        setDataAndType(uri, "application/pdf")
                        addFlags(android.content.Intent.FLAG_GRANT_READ_URI_PERMISSION)
                        addFlags(android.content.Intent.FLAG_ACTIVITY_NEW_TASK)
                    }
                    context.startActivity(intent)
                    _uiState.value = _uiState.value.copy(message = "Relevé PDF téléchargé")
                } catch (e: Exception) {
                    _uiState.value = _uiState.value.copy(error = "Impossible d'ouvrir le PDF")
                }
            },
            onFailure = { _uiState.value = _uiState.value.copy(error = "Téléchargement échoué: ${it.message}") }
        )
    }

    // ── Crypto ──────────────────────────────────────────────────────────────

    fun loadCrypto() = viewModelScope.launch {
        _uiState.value = _uiState.value.copy(cryptoLoading = true)
        val wallets = repository.getCryptoWallets()
        val markets = repository.getCoinMarkets()
        _uiState.value = _uiState.value.copy(
            cryptoLoading = false,
            cryptoWallets = wallets.getOrDefault(emptyList()),
            cryptoMarkets = markets.getOrDefault(emptyList()),
            error = if (markets.isFailure) "Impossible de charger les prix crypto" else null
        )
    }

    fun loadCryptoChart(coinId: String, days: String = "1") = viewModelScope.launch {
        repository.getCoinChart(coinId, days).fold(
            onSuccess = { _uiState.value = _uiState.value.copy(cryptoChart = it) },
            onFailure = { _uiState.value = _uiState.value.copy(cryptoChart = emptyList()) }
        )
    }

    fun buyCrypto(symbol: String, amountMga: Double, priceUsd: Double) = viewModelScope.launch {
        val mga = _uiState.value.mgaPerUsd
        repository.buyCrypto(symbol, amountMga, priceUsd, mga).fold(
            onSuccess = {
                _uiState.value = _uiState.value.copy(message = "Achat $symbol effectué ✓")
                loadCrypto()
                refreshAll()
            },
            onFailure = { _uiState.value = _uiState.value.copy(error = it.message) }
        )
    }

    fun sellCrypto(symbol: String, cryptoAmount: Double, priceUsd: Double) = viewModelScope.launch {
        val mga = _uiState.value.mgaPerUsd
        repository.sellCrypto(symbol, cryptoAmount, priceUsd, mga).fold(
            onSuccess = {
                val totalMga = it.totalMga ?: (cryptoAmount * priceUsd * mga)
                _uiState.value = _uiState.value.copy(
                    message = "Vente $symbol → ${String.format("%,.0f", totalMga)} MGA ✓"
                )
                loadCrypto()
                refreshAll()
            },
            onFailure = { _uiState.value = _uiState.value.copy(error = it.message) }
        )
    }

    fun sendCrypto(symbol: String, cryptoAmount: Double, toAddress: String, priceUsd: Double) = viewModelScope.launch {
        val mga = _uiState.value.mgaPerUsd
        repository.sendCrypto(symbol, cryptoAmount, toAddress, priceUsd, mga).fold(
            onSuccess = { _uiState.value = _uiState.value.copy(message = "Envoi $symbol confirmé ✓") },
            onFailure = { _uiState.value = _uiState.value.copy(error = it.message) }
        )
        loadCrypto()
    }

    // ── KYC ─────────────────────────────────────────────────────────────────

    fun submitKyc(cinFullName: String, cinRectoUri: Uri, cinVersoUri: Uri) = viewModelScope.launch {
        _uiState.value = _uiState.value.copy(kycSubmitting = true, error = null)
        repository.submitKyc(cinFullName, cinRectoUri, cinVersoUri).fold(
            onSuccess = {
                val updatedUser = _uiState.value.user?.copy(kycStatus = "pending")
                _uiState.value = _uiState.value.copy(
                    user = updatedUser,
                    kycSubmitting = false,
                    message = "Documents envoyés. Votre vérification est en cours."
                )
            },
            onFailure = {
                _uiState.value = _uiState.value.copy(kycSubmitting = false, error = it.message)
            }
        )
    }

    fun consumeMessages() {
        _uiState.value = _uiState.value.copy(message = null, error = null)
    }

    fun logout() = viewModelScope.launch {
        repository.logout()
        _uiState.value = BankUiState(mockMode = repository.useMockData)
    }
}

class BankViewModelFactory(
    private val repository: BankRepository,
    private val appContext: Context? = null
) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T =
        BankViewModel(repository, appContext) as T
}

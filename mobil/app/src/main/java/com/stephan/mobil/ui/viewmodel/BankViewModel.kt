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
        val initializing: Boolean = true,
        val loading: Boolean = false,
        val user: User? = null,
        val balance: BalanceResponse = BalanceResponse(),
        val transactions: List<Transaction> = emptyList(),
        val beneficiaries: List<Beneficiary> = emptyList(),
        val cards: List<Card> = emptyList(),
        val pendingTransfer: Transfer? = null,
        val qrPayload: String? = null,
        val qrScanResult: QrScanResult? = null,
        val revealedCard: com.stephan.mobil.data.model.CardDetails? = null,
        val message: String? = null,
        val error: String? = null,
        val mockMode: Boolean = false,
        val notifications: List<com.stephan.mobil.data.model.AppNotification> = emptyList(),
        val pendingCardPayments: List<com.stephan.mobil.data.model.PendingCardPayment> = emptyList(),
        val kycRejectionReason: String? = null,
        val kycSubmitting: Boolean = false,
        val depositPending: DepositResult? = null,
        val depositSuccess: DepositResult? = null,
        val isDepositing: Boolean = false,
        val isConfirmingDeposit: Boolean = false,
        val scheduledWithdrawals: List<com.stephan.mobil.data.model.ScheduledWithdrawal> = emptyList(),
    )

class BankViewModel(private val repository: BankRepository, private val appContext: Context? = null) : ViewModel() {
    private val _uiState = MutableStateFlow(BankUiState(mockMode = repository.useMockData))
    val uiState: StateFlow<BankUiState> = _uiState.asStateFlow()

    init {
        if (repository.useMockData) {
            _uiState.value = _uiState.value.copy(initializing = false)
        } else {
            restoreSessionOrRefresh()
        }
    }

    private fun restoreSessionOrRefresh() = viewModelScope.launch {
        val token = appContext?.let { com.stephan.mobil.security.SecurityUtil.getAuthToken(it) }
        if (!token.isNullOrBlank() && !repository.useMockData) {
            val result = repository.getCurrentUser()
            if (result.isSuccess) {
                val user = result.getOrThrow()
                val kycInfo = repository.getKycStatus().getOrNull()
                _uiState.value = _uiState.value.copy(
                    initializing = false,
                    user = if (kycInfo != null) user.copy(kycStatus = kycInfo.status) else user,
                    kycRejectionReason = kycInfo?.rejectionReason
                )
                refreshAll()
                return@launch
            } else {
                appContext?.let { com.stephan.mobil.security.SecurityUtil.clearData(it) }
            }
        }
        _uiState.value = _uiState.value.copy(initializing = false, loading = false)
    }

    fun setMockMode(enabled: Boolean) {
        repository.useMockData = enabled
        _uiState.value = _uiState.value.copy(mockMode = enabled, message = if (enabled) "Mode mock active" else "Mode API active")
        refreshAll()
    }

    fun login(email: String, password: String) = viewModelScope.launch {
        _uiState.value = _uiState.value.copy(loading = true, error = null)
        repository.login(email, password).fold(
            onSuccess = { user ->
                val kycInfo = if (!repository.useMockData) repository.getKycStatus().getOrNull() else null
                _uiState.value = _uiState.value.copy(
                    loading = false,
                    user = if (kycInfo != null) user.copy(kycStatus = kycInfo.status) else user,
                    kycRejectionReason = kycInfo?.rejectionReason,
                    message = "Connecte"
                )
                refreshAll()
            },
            onFailure = {
                _uiState.value = _uiState.value.copy(loading = false, error = it.message ?: "Connexion impossible")
            }
        )
    }

    fun register(name: String, email: String, phone: String, password: String) = viewModelScope.launch {
        _uiState.value = _uiState.value.copy(loading = true, error = null)
        repository.register(name, email, phone, password).fold(
            onSuccess = {
                _uiState.value = _uiState.value.copy(loading = false, user = it, message = "Compte cree")
                refreshAll()
            },
            onFailure = {
                _uiState.value = _uiState.value.copy(loading = false, error = it.message ?: "Inscription impossible")
            }
        )
    }

    fun refreshAll() = viewModelScope.launch {
        _uiState.value = _uiState.value.copy(loading = true, error = null)
        val balance = repository.getBalance()
        val transactions = repository.getTransactions()
        val beneficiaries = repository.getBeneficiaries()
        val cards = repository.getCards()
        val scheduledWithdrawals = repository.getScheduledWithdrawals()

        _uiState.value = _uiState.value.copy(
            loading = false,
            balance = balance.getOrDefault(BalanceResponse()),
            transactions = transactions.getOrDefault(emptyList()),
            beneficiaries = beneficiaries.getOrDefault(emptyList()),
            cards = cards.getOrDefault(emptyList()),
            scheduledWithdrawals = scheduledWithdrawals.getOrDefault(emptyList()),
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

    fun deposit(accountId: Int, amount: Double, method: String, phone: String?) = viewModelScope.launch {
        _uiState.value = _uiState.value.copy(isDepositing = true, depositPending = null, depositSuccess = null)
        repository.deposit(DepositRequest(accountId, amount, method, phone.takeIf { !it.isNullOrBlank() })).fold(
            onSuccess = {
                _uiState.value = _uiState.value.copy(isDepositing = false, depositPending = it)
            },
            onFailure = {
                _uiState.value = _uiState.value.copy(isDepositing = false, error = it.message)
            }
        )
    }

    fun confirmDeposit() = viewModelScope.launch {
        val reference = _uiState.value.depositPending?.reference ?: return@launch
        _uiState.value = _uiState.value.copy(isConfirmingDeposit = true)
        repository.confirmDeposit(reference).fold(
            onSuccess = {
                _uiState.value = _uiState.value.copy(
                    isConfirmingDeposit = false,
                    depositPending = null,
                    depositSuccess = it,
                )
                refreshAll()
            },
            onFailure = {
                _uiState.value = _uiState.value.copy(isConfirmingDeposit = false, error = it.message)
            }
        )
    }

    fun cancelDeposit() = viewModelScope.launch {
        val reference = _uiState.value.depositPending?.reference ?: return@launch
        repository.cancelDeposit(reference).fold(
            onSuccess = { _uiState.value = _uiState.value.copy(depositPending = null) },
            onFailure = { _uiState.value = _uiState.value.copy(depositPending = null) }
        )
    }

    fun clearDepositResult() {
        _uiState.value = _uiState.value.copy(depositPending = null, depositSuccess = null)
    }

    fun createScheduledWithdrawal(
        senderAccountId: Int,
        beneficiaryId: Int,
        amount: Double,
        note: String,
        frequencyDays: Int
    ) = viewModelScope.launch {
        _uiState.value = _uiState.value.copy(loading = true, error = null)
        repository.createScheduledWithdrawal(
            ScheduledWithdrawalRequest(senderAccountId, beneficiaryId, amount, note.ifBlank { null }, frequencyDays)
        ).fold(
            onSuccess = {
                _uiState.value = _uiState.value.copy(
                    loading = false,
                    message = "Retrait automatique programmé tous les $frequencyDays jour(s).",
                    scheduledWithdrawals = _uiState.value.scheduledWithdrawals + it
                )
            },
            onFailure = { _uiState.value = _uiState.value.copy(loading = false, error = it.message) }
        )
    }

    fun toggleScheduledWithdrawal(id: Int) = viewModelScope.launch {
        repository.toggleScheduledWithdrawal(id).fold(
            onSuccess = { updated ->
                _uiState.value = _uiState.value.copy(
                    scheduledWithdrawals = _uiState.value.scheduledWithdrawals.map { if (it.id == id) updated else it }
                )
            },
            onFailure = { _uiState.value = _uiState.value.copy(error = it.message) }
        )
    }

    fun deleteScheduledWithdrawal(id: Int) = viewModelScope.launch {
        repository.deleteScheduledWithdrawal(id).fold(
            onSuccess = {
                _uiState.value = _uiState.value.copy(
                    scheduledWithdrawals = _uiState.value.scheduledWithdrawals.filter { it.id != id },
                    message = "Retrait automatique supprimé."
                )
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

    fun updateCardLimit(cardId: Int, limit: Double) = viewModelScope.launch {
        repository.updateCardLimit(cardId, limit).fold(
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

    fun deleteCard(cardId: Int) = viewModelScope.launch {
        repository.deleteCard(cardId).fold(
            onSuccess = { refreshAll() },
            onFailure = { _uiState.value = _uiState.value.copy(error = it.message) }
        )
    }

    fun revealCard(cardId: Int) = viewModelScope.launch {
        repository.revealCard(cardId).fold(
            onSuccess = { _uiState.value = _uiState.value.copy(revealedCard = it) },
            onFailure = { _uiState.value = _uiState.value.copy(error = it.message) }
        )
    }

    fun clearRevealedCard() {
        _uiState.value = _uiState.value.copy(revealedCard = null)
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

    // ── Notifications ────────────────────────────────────────────────────────

    fun loadNotifications() = viewModelScope.launch {
        repository.getNotifications().fold(
            onSuccess = { _uiState.value = _uiState.value.copy(notifications = it) },
            onFailure = { }
        )
    }

    // ── Card payment confirmation ─────────────────────────────────────────────

    fun loadPendingCardPayments() = viewModelScope.launch {
        repository.getPendingCardPayments().fold(
            onSuccess = { _uiState.value = _uiState.value.copy(pendingCardPayments = it) },
            onFailure = { }
        )
    }

    fun confirmCardPayment(reference: String) = viewModelScope.launch {
        repository.confirmCardPayment(reference).fold(
            onSuccess = {
                _uiState.value = _uiState.value.copy(
                    pendingCardPayments = _uiState.value.pendingCardPayments.filter { it.reference != reference },
                    message = "Paiement confirmé ✓"
                )
                refreshAll()
            },
            onFailure = { _uiState.value = _uiState.value.copy(error = it.message) }
        )
    }

    fun declineCardPayment(reference: String) = viewModelScope.launch {
        repository.declineCardPayment(reference).fold(
            onSuccess = {
                _uiState.value = _uiState.value.copy(
                    pendingCardPayments = _uiState.value.pendingCardPayments.filter { it.reference != reference },
                    message = "Paiement refusé"
                )
            },
            onFailure = { _uiState.value = _uiState.value.copy(error = it.message) }
        )
    }

    fun markNotificationRead(id: Int) = viewModelScope.launch {
        repository.markNotificationRead(id).fold(
            onSuccess = { loadNotifications() },
            onFailure = { }
        )
    }

    fun markAllNotificationsRead() = viewModelScope.launch {
        repository.markAllNotificationsRead().fold(
            onSuccess = { loadNotifications() },
            onFailure = { }
        )
    }

    // ── Profile ─────────────────────────────────────────────────────────────

    fun updateProfile(currentPassword: String, name: String? = null, email: String? = null, phone: String? = null) = viewModelScope.launch {
        _uiState.value = _uiState.value.copy(loading = true, error = null)
        repository.updateProfile(currentPassword, name, email, phone).fold(
            onSuccess = { updatedUser ->
                _uiState.value = _uiState.value.copy(loading = false, user = updatedUser, message = "Profil mis à jour.")
            },
            onFailure = { _uiState.value = _uiState.value.copy(loading = false, error = it.message) }
        )
    }

    // ── KYC ─────────────────────────────────────────────────────────────────

    fun refreshKycStatus() = viewModelScope.launch {
        val kycInfo = repository.getKycStatus().getOrNull() ?: return@launch
        val currentUser = _uiState.value.user ?: return@launch
        _uiState.value = _uiState.value.copy(
            user = currentUser.copy(kycStatus = kycInfo.status),
            kycRejectionReason = kycInfo.rejectionReason
        )
    }

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
        _uiState.value = BankUiState(initializing = false, loading = false)
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

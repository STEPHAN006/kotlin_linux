package com.stephan.mobil.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.stephan.mobil.data.model.*
import com.stephan.mobil.data.repository.BankRepository
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
    val message: String? = null,
    val error: String? = null,
    val mockMode: Boolean = true
)

class BankViewModel(private val repository: BankRepository) : ViewModel() {
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
            onSuccess = { _uiState.value = _uiState.value.copy(user = it, message = "Connecte") },
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
        repository.verifyOtp(reference, otp).fold(
            onSuccess = {
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
        repository.scanQr(payload).fold(
            onSuccess = { _uiState.value = _uiState.value.copy(message = "QR valide: pret pour paiement") },
            onFailure = { _uiState.value = _uiState.value.copy(error = it.message) }
        )
    }

    fun payQr(senderAccountId: Int, payload: String, amount: Double) = viewModelScope.launch {
        repository.payQr(senderAccountId, payload, amount).fold(
            onSuccess = {
                _uiState.value = _uiState.value.copy(
                    pendingTransfer = if (it.otpRequired) it.transfer else null,
                    message = if (it.otpRequired) "OTP requis pour finaliser le paiement QR" else "Paiement QR envoye"
                )
                refreshAll()
            },
            onFailure = { _uiState.value = _uiState.value.copy(error = it.message) }
        )
    }

    fun consumeMessages() {
        _uiState.value = _uiState.value.copy(message = null, error = null)
    }
}

class BankViewModelFactory(private val repository: BankRepository) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T = BankViewModel(repository) as T
}

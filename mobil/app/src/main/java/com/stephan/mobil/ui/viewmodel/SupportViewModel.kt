package com.stephan.mobil.ui.viewmodel

import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.stephan.mobil.data.model.SupportTicketDetail
import com.stephan.mobil.data.repository.BankRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class SupportUiState(
    val supportTicket: SupportTicketDetail? = null,
    val supportLoading: Boolean = false,
    val message: String? = null,
    val error: String? = null,
)

class SupportViewModel(private val repository: BankRepository) : ViewModel() {

    private val _uiState = MutableStateFlow(SupportUiState())
    val uiState: StateFlow<SupportUiState> = _uiState.asStateFlow()

    fun loadSupportTicket() = viewModelScope.launch {
        _uiState.value = _uiState.value.copy(supportLoading = true)
        repository.getOrCreateSupportTicket().fold(
            onSuccess = { _uiState.value = _uiState.value.copy(supportTicket = it, supportLoading = false) },
            onFailure = { _uiState.value = _uiState.value.copy(supportLoading = false, error = it.message) }
        )
    }

    fun sendSupportMessage(message: String, imageUri: Uri? = null) = viewModelScope.launch {
        val ticket = _uiState.value.supportTicket ?: return@launch
        repository.sendSupportMessage(ticket.id, message, imageUri).fold(
            onSuccess = { _uiState.value = _uiState.value.copy(supportTicket = it) },
            onFailure = { _uiState.value = _uiState.value.copy(error = it.message) }
        )
    }

    fun closeTicket() = viewModelScope.launch {
        val ticket = _uiState.value.supportTicket ?: return@launch
        _uiState.value = _uiState.value.copy(supportLoading = true)
        repository.closeSupportTicket(ticket.id).fold(
            onSuccess = { _uiState.value = _uiState.value.copy(supportTicket = it, supportLoading = false) },
            onFailure = { _uiState.value = _uiState.value.copy(supportLoading = false, error = it.message) }
        )
    }

    fun startNewTicket() = viewModelScope.launch {
        _uiState.value = _uiState.value.copy(supportLoading = true, supportTicket = null)
        repository.getOrCreateSupportTicket().fold(
            onSuccess = { _uiState.value = _uiState.value.copy(supportTicket = it, supportLoading = false) },
            onFailure = { _uiState.value = _uiState.value.copy(supportLoading = false, error = it.message) }
        )
    }

    fun consumeMessages() {
        _uiState.value = _uiState.value.copy(message = null, error = null)
    }
}

class SupportViewModelFactory(
    private val repository: BankRepository
) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T =
        SupportViewModel(repository) as T
}

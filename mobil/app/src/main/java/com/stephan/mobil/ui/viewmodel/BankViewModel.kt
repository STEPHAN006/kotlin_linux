package com.stephan.mobil.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.stephan.mobil.data.model.BalanceResponse
import com.stephan.mobil.data.model.Transaction
import com.stephan.mobil.data.repository.BankRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

sealed class BankUiState {
    object Loading : BankUiState()
    data class Success(val balance: BalanceResponse, val transactions: List<Transaction>) : BankUiState()
    data class Error(val message: String) : BankUiState()
}

class BankViewModel(private val repository: BankRepository) : ViewModel() {

    private val _uiState = MutableStateFlow<BankUiState>(BankUiState.Loading)
    val uiState: StateFlow<BankUiState> = _uiState.asStateFlow()

    init {
        fetchDashboardData()
    }

    fun fetchDashboardData() {
        _uiState.value = BankUiState.Loading
        viewModelScope.launch {
            val balanceResult = repository.getBalance()
            val transactionsResult = repository.getTransactions()

            if (balanceResult.isSuccess && transactionsResult.isSuccess) {
                _uiState.value = BankUiState.Success(
                    balance = balanceResult.getOrNull()!!,
                    transactions = transactionsResult.getOrNull()!!
                )
            } else {
                _uiState.value = BankUiState.Error("Erreur lors du chargement des données bancaires")
            }
        }
    }
    
    // Note UI (Compose) pour l'affichage (débits rouge, crédits vert):
    // Dans l'écran Compose, on itère sur les transactions.
    // val color = if (transaction.isCredit) Color.Green else Color.Red
    // Text(text = "${transaction.amount}", color = color)
}

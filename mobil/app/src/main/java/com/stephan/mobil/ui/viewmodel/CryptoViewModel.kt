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

data class CryptoUiState(
    val cryptoWallets: List<CryptoWallet> = emptyList(),
    val cryptoMarkets: List<CoinMarketData> = emptyList(),
    val cryptoChart: List<Pair<Long, Double>> = emptyList(),
    val cryptoCandleData: List<List<Double>> = emptyList(),
    val cryptoLoading: Boolean = false,
    val exchangeRates: Map<String, Double> = emptyMap(),
    val mgaPerUsd: Double = 4500.0,
    val mgaPerEur: Double = 4891.0,
    val cryptoTxns: List<CryptoTxn> = emptyList(),
    val message: String? = null,
    val error: String? = null,
)

class CryptoViewModel(
    private val repository: BankRepository,
    private val onBalanceChanged: () -> Unit = {}
) : ViewModel() {

    private val _uiState = MutableStateFlow(CryptoUiState())
    val uiState: StateFlow<CryptoUiState> = _uiState.asStateFlow()

    fun loadCrypto() = viewModelScope.launch {
        _uiState.value = _uiState.value.copy(cryptoLoading = true)
        val wallets = repository.getCryptoWallets()
        val markets = repository.getCoinMarkets()
        _uiState.value = _uiState.value.copy(
            cryptoLoading = false,
            cryptoWallets = wallets.getOrDefault(emptyList()),
            cryptoMarkets = markets.getOrDefault(emptyList()),
            error = if (markets.isFailure) "Impossible de charger les prix crypto" else null,
        )
    }

    fun loadCryptoChart(coinId: String, days: String = "1") = viewModelScope.launch {
        repository.getCoinChart(coinId, days).fold(
            onSuccess = { _uiState.value = _uiState.value.copy(cryptoChart = it) },
            onFailure = { _uiState.value = _uiState.value.copy(cryptoChart = emptyList()) }
        )
    }

    fun loadExchangeRates() = viewModelScope.launch {
        repository.getExchangeRates().fold(
            onSuccess = {
                val mga = it["MGA"] ?: 4500.0
                val eur = it["EUR"] ?: 0.92
                _uiState.value = _uiState.value.copy(
                    exchangeRates = it,
                    mgaPerUsd = mga,
                    mgaPerEur = if (eur > 0) mga / eur else 4891.0
                )
            },
            onFailure = { }
        )
    }

    fun loadCryptoCandles(coinId: String, days: String = "7") = viewModelScope.launch {
        repository.getCoinOhlc(coinId, days).fold(
            onSuccess = { _uiState.value = _uiState.value.copy(cryptoCandleData = it) },
            onFailure = { _uiState.value = _uiState.value.copy(cryptoCandleData = emptyList()) }
        )
    }

    fun loadCryptoTransactions() = viewModelScope.launch {
        repository.getCryptoTransactions().fold(
            onSuccess = { _uiState.value = _uiState.value.copy(cryptoTxns = it) },
            onFailure = { }
        )
    }

    fun buyCrypto(symbol: String, amountMga: Double, priceUsd: Double, onResult: (Boolean) -> Unit = {}) = viewModelScope.launch {
        val mga = _uiState.value.mgaPerUsd
        repository.buyCrypto(symbol, amountMga, priceUsd, mga).fold(
            onSuccess = {
                _uiState.value = _uiState.value.copy(message = "Achat $symbol effectué ✓")
                onResult(true)
                loadCrypto()
                onBalanceChanged()
            },
            onFailure = {
                _uiState.value = _uiState.value.copy(error = it.message)
                onResult(false)
            }
        )
    }

    fun sellCrypto(symbol: String, cryptoAmount: Double, priceUsd: Double, onResult: (Boolean) -> Unit = {}) = viewModelScope.launch {
        val mga = _uiState.value.mgaPerUsd
        repository.sellCrypto(symbol, cryptoAmount, priceUsd, mga).fold(
            onSuccess = { result ->
                val totalMga = result.totalMga ?: (cryptoAmount * priceUsd * mga)
                _uiState.value = _uiState.value.copy(
                    message = "Vente $symbol → ${String.format("%,.0f", totalMga)} MGA ✓"
                )
                onResult(true)
                loadCrypto()
                onBalanceChanged()
            },
            onFailure = {
                _uiState.value = _uiState.value.copy(error = it.message)
                onResult(false)
            }
        )
    }

    fun sendCrypto(symbol: String, cryptoAmount: Double, toAddress: String, priceUsd: Double, onResult: (Boolean) -> Unit = {}) = viewModelScope.launch {
        val mga = _uiState.value.mgaPerUsd
        repository.sendCrypto(symbol, cryptoAmount, toAddress, priceUsd, mga).fold(
            onSuccess = {
                _uiState.value = _uiState.value.copy(message = "Envoi $symbol confirmé ✓")
                onResult(true)
            },
            onFailure = {
                _uiState.value = _uiState.value.copy(error = it.message)
                onResult(false)
            }
        )
        loadCrypto()
    }

    fun swapCrypto(fromSymbol: String, fromAmount: Double, fromPriceUsd: Double, toSymbol: String, toPriceUsd: Double, onResult: (Boolean) -> Unit = {}) = viewModelScope.launch {
        val mga = _uiState.value.mgaPerUsd
        repository.swapCrypto(fromSymbol, fromAmount, fromPriceUsd, toSymbol, toPriceUsd, mga).fold(
            onSuccess = { result ->
                _uiState.value = _uiState.value.copy(
                    message = "Swap $fromSymbol → $toSymbol effectué ✓"
                )
                onResult(true)
                loadCrypto()
                onBalanceChanged()
            },
            onFailure = {
                _uiState.value = _uiState.value.copy(error = it.message)
                onResult(false)
            }
        )
    }

    fun consumeMessages() {
        _uiState.value = _uiState.value.copy(message = null, error = null)
    }
}

class CryptoViewModelFactory(
    private val repository: BankRepository,
    private val onBalanceChanged: () -> Unit = {}
) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T =
        CryptoViewModel(repository, onBalanceChanged) as T
}

package com.example.marketdata.presentation

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.marketdata.domain.model.Instrument
import com.example.marketdata.domain.model.MarketData
import com.example.marketdata.domain.usecase.*
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class MarketDataViewModel(
    private val getTokenUseCase: GetTokenUseCase,
    private val getInstrumentsUseCase: GetInstrumentsUseCase,
    private val getInstrumentBySymbolUseCase: GetInstrumentBySymbolUseCase,
    private val getHistoricalDataUseCase: GetHistoricalDataUseCase,
    private val getRealTimeMarketDataUseCase: GetRealTimeMarketDataUseCase
) : ViewModel() {

    private val _isLoading = MutableStateFlow(false)
    val isLoading = _isLoading.asStateFlow()

    private val _marketData = MutableStateFlow<MarketData?>(null)
    val marketData = _marketData.asStateFlow()

    private val _symbol = MutableStateFlow<String?>(null)
    val symbol = _symbol.asStateFlow()

    private val _historicalData = MutableStateFlow<List<Instrument>>(emptyList())
    val historicalData = _historicalData.asStateFlow()

    private var currentSubscriptionJob: Job? = null // Track the current subscription job

    fun fetchMarketDataForSymbol(symbol: String) {
        // Cancel any existing job before starting a new one
        currentSubscriptionJob?.cancel()

        currentSubscriptionJob = viewModelScope.launch {
            _isLoading.value = true
            _marketData.value = null  // Clear previous data

            val token = getTokenUseCase()
            if (token != null) {
                val instrument = getInstrumentBySymbolUseCase(token, symbol)
                if (instrument != null) {
                    _symbol.value = instrument.symbol
                    getRealTimeMarketDataUseCase(token, instrument.id).collect { data ->
                        _marketData.value = data
                        _isLoading.value = false
                    }
                } else {
                    _isLoading.value = false
                    Log.e("MarketDataViewModel", "Instrument with symbol $symbol not found.")
                }
            } else {
                _isLoading.value = false
                Log.e("MarketDataViewModel", "Failed to retrieve token.")
            }
        }
    }

    suspend fun getInstrumentIdForSymbol(symbol: String): String? {
        val token = getTokenUseCase()
        return if (token != null) {
            getInstrumentBySymbolUseCase(token, symbol)?.id
        } else {
            Log.e("MarketDataViewModel", "Failed to retrieve token.")
            null
        }
    }

    suspend fun getAllSymbols(): List<String> {
        val token = getTokenUseCase()
        return if (token != null) {
            getInstrumentsUseCase(token).map { it.symbol }
        } else {
            emptyList()
        }
    }

    fun fetchHistoricalData(instrumentId: String) {
        viewModelScope.launch {
            _isLoading.value = true
            val token = getTokenUseCase()
            if (token != null) {
                val data = getHistoricalDataUseCase(token, instrumentId)
                _historicalData.value = data
                _isLoading.value = false
            } else {
                _isLoading.value = false
                Log.e("MarketDataViewModel", "Failed to retrieve token.")
            }
        }
    }

    override fun onCleared() {
        super.onCleared()
        // Cancel any active WebSocket connection when ViewModel is destroyed
        currentSubscriptionJob?.cancel()
    }
}
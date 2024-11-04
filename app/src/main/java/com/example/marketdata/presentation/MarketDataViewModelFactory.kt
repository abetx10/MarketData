package com.example.marketdata.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.marketdata.domain.repository.MarketDataRepository
import com.example.marketdata.domain.usecase.*

class MarketDataViewModelFactory(private val repository: MarketDataRepository) : ViewModelProvider.Factory {

    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(MarketDataViewModel::class.java)) {
            return MarketDataViewModel(
                getTokenUseCase = GetTokenUseCase(repository),
                getInstrumentsUseCase = GetInstrumentsUseCase(repository),
                getInstrumentBySymbolUseCase = GetInstrumentBySymbolUseCase(repository),
                getHistoricalDataUseCase = GetHistoricalDataUseCase(repository),
                getRealTimeMarketDataUseCase = GetRealTimeMarketDataUseCase(repository)
            ) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
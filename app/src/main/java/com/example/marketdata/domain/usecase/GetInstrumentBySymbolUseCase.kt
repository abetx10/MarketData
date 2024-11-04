package com.example.marketdata.domain.usecase

import com.example.marketdata.domain.model.Instrument
import com.example.marketdata.domain.repository.MarketDataRepository

class GetInstrumentBySymbolUseCase(private val repository: MarketDataRepository) {
    suspend operator fun invoke(token: String, symbol: String): Instrument? {
        return repository.getInstrumentBySymbol(token, symbol)
    }
}
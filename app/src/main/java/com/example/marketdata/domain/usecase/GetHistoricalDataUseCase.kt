package com.example.marketdata.domain.usecase

import com.example.marketdata.domain.model.Instrument
import com.example.marketdata.domain.repository.MarketDataRepository

class GetHistoricalDataUseCase(private val repository: MarketDataRepository) {
    suspend operator fun invoke(token: String, instrumentId: String): List<Instrument> {
        return repository.getHistoricalData(token, instrumentId)
    }
}
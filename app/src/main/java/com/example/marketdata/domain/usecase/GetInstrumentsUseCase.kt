package com.example.marketdata.domain.usecase

import com.example.marketdata.domain.model.Instrument
import com.example.marketdata.domain.repository.MarketDataRepository

class GetInstrumentsUseCase(private val repository: MarketDataRepository) {
    suspend operator fun invoke(token: String): List<Instrument> {
        return repository.getInstruments(token)
    }
}
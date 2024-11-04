package com.example.marketdata.domain.usecase

import com.example.marketdata.domain.model.MarketData
import com.example.marketdata.domain.repository.MarketDataRepository
import kotlinx.coroutines.flow.Flow

class GetRealTimeMarketDataUseCase(private val repository: MarketDataRepository) {
    operator fun invoke(token: String, instrumentId: String): Flow<MarketData?> {
        return repository.getRealTimeMarketData(token, instrumentId)
    }
}
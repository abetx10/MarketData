package com.example.marketdata.domain.usecase

import com.example.marketdata.domain.repository.MarketDataRepository

class GetTokenUseCase(private val repository: MarketDataRepository) {
    suspend operator fun invoke(): String? {
        return repository.getToken()
    }
}
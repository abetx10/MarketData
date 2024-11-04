package com.example.marketdata.domain.repository

import com.example.marketdata.domain.model.Instrument
import com.example.marketdata.domain.model.MarketData
import kotlinx.coroutines.flow.Flow

interface MarketDataRepository {
    suspend fun getToken(): String?
    suspend fun getInstruments(token: String): List<Instrument>
    suspend fun getInstrumentBySymbol(token: String, symbol: String): Instrument?
    suspend fun getHistoricalData(token: String, instrumentId: String): List<Instrument>
    fun getRealTimeMarketData(token: String, instrumentId: String): Flow<MarketData?>
}
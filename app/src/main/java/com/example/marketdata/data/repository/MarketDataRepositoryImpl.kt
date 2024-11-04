package com.example.marketdata.data.repository

import android.util.Log
import com.example.marketdata.data.network.ApiService
import com.example.marketdata.data.websocket.WebSocketClient
import com.example.marketdata.domain.model.Instrument
import com.example.marketdata.domain.model.MarketData
import com.example.marketdata.domain.repository.MarketDataRepository
import kotlinx.coroutines.flow.Flow

class MarketDataRepositoryImpl(private val apiService: ApiService) : MarketDataRepository {

    private var instrumentsCache: List<Instrument> = emptyList()

    override suspend fun getToken(): String? {
        val response = apiService.getToken()
        return if (response.isSuccessful) {
            response.body()?.access_token
        } else {
            Log.e("Repository", "Error fetching token: ${response.errorBody()}")
            null
        }
    }

    override suspend fun getInstruments(token: String): List<Instrument> {
        if (instrumentsCache.isEmpty()) {
            val response = apiService.getInstruments("Bearer $token")
            if (response.isSuccessful) {
                instrumentsCache = response.body()?.data ?: emptyList()
            }
        }
        return instrumentsCache
    }

    override suspend fun getInstrumentBySymbol(token: String, symbol: String): Instrument? {
        return getInstruments(token).find { it.symbol == symbol }
    }

    override suspend fun getHistoricalData(token: String, instrumentId: String): List<Instrument> {
        val response = apiService.getHistoricalData("Bearer $token", instrumentId)
        return if (response.isSuccessful) {
            response.body()?.data ?: emptyList()
        } else {
            Log.e("Repository", "Error fetching historical data: ${response.errorBody()}")
            emptyList()
        }
    }

    override fun getRealTimeMarketData(token: String, instrumentId: String): Flow<MarketData?> {
        val webSocketClient = WebSocketClient(token)
        return webSocketClient.connect(instrumentId)
    }
}
package com.example.marketdata.domain.model

data class MarketData(
    val symbol: String,
    val price: Double,
    val timestamp: String
)
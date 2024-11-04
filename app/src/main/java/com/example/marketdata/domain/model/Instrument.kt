package com.example.marketdata.domain.model

data class Instrument(
    val id: String,         // Instrument identifier
    val symbol: String,      // Instrument symbol
    val t: String?,          // Time (for historical data)
    val o: Double?,          // Opening price
    val h: Double?,          // Highest price
    val l: Double?,          // Lowest price
    val c: Double?,          // Closing price
    val v: Int?              // Volume
)
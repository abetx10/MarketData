package com.example.marketdata.data.websocket

import android.util.Log
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.WebSocket
import okhttp3.WebSocketListener
import com.example.marketdata.domain.model.MarketData
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import org.json.JSONException
import org.json.JSONObject
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter
import java.time.ZoneId

class WebSocketClient(private val token: String) {

    private var webSocket: WebSocket? = null
    private var currentInstrumentId: String? = null  // Track current subscribed instrument ID

    fun connect(instrumentId: String): Flow<MarketData?> = callbackFlow {
        val client = OkHttpClient()

        // Unsubscribe from previous symbol if one exists
        if (currentInstrumentId != null) {
            val unsubscribeMessage = """
                {
                    "type": "l1-subscription",
                    "id": "1",
                    "instrumentId": "$currentInstrumentId",
                    "provider": "simulation",
                    "subscribe": false
                }
            """.trimIndent()
            webSocket?.send(unsubscribeMessage)
            Log.d("WebSocket", "Unsubscribing from previous instrumentId: $currentInstrumentId")
        }

        // Close previous WebSocket connection if exists
        webSocket?.close(1000, "Closing previous connection")

        // Save new instrument ID
        currentInstrumentId = instrumentId

        val request = Request.Builder()
            .url("wss://platform.fintacharts.com/api/streaming/ws/v1/realtime?token=$token")
            .build()

        val listener = object : WebSocketListener() {
            override fun onOpen(webSocket: WebSocket, response: okhttp3.Response) {
                Log.d("WebSocket", "Connected to WebSocket.")
                val subscribeMessage = """
                    {
                        "type": "l1-subscription",
                        "id": "1",
                        "instrumentId": "$instrumentId",
                        "provider": "simulation",
                        "subscribe": true,
                        "kinds": ["ask", "bid", "last"]
                    }
                """.trimIndent()
                Log.d("WebSocket", "Sending subscribe message: $subscribeMessage")
                webSocket.send(subscribeMessage)
            }

            override fun onMessage(webSocket: WebSocket, text: String) {
                Log.d("WebSocket", "Received message: $text")
                try {
                    val json = JSONObject(text)
                    val messageType = json.optString("type", "")

                    if (messageType == "l1-update") {
                        val instrumentId = json.optString("instrumentId", "unknown")
                        var price = -1.0
                        var timestamp = "N/A"

                        json.optJSONObject("last")?.let {
                            price = it.optDouble("price", -1.0)
                            timestamp = it.optString("timestamp", "N/A")
                        }
                        if (price == -1.0) {
                            json.optJSONObject("ask")?.let {
                                price = it.optDouble("price", -1.0)
                                timestamp = it.optString("timestamp", "N/A")
                            }
                        }
                        if (price == -1.0) {
                            json.optJSONObject("bid")?.let {
                                price = it.optDouble("price", -1.0)
                                timestamp = it.optString("timestamp", "N/A")
                            }
                        }

                        if (price != -1.0) {
                            val formattedTimestamp = if (timestamp != "N/A") {
                                try {
                                    val utcDateTime = ZonedDateTime.parse(timestamp)
                                    val localDateTime = utcDateTime.withZoneSameInstant(ZoneId.systemDefault())
                                    localDateTime.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"))
                                } catch (e: Exception) {
                                    Log.e("WebSocket", "Error formatting timestamp: ${e.localizedMessage}")
                                    "Invalid date"
                                }
                            } else {
                                "Timestamp unavailable"
                            }

                            val marketData = MarketData(
                                symbol = instrumentId,
                                price = price,
                                timestamp = formattedTimestamp
                            )
                            trySend(marketData)
                        } else {
                            Log.w("WebSocket", "Price field is missing in the received message.")
                        }
                    } else {
                        Log.d("WebSocket", "Ignored non-market data message of type: $messageType")
                    }
                } catch (e: JSONException) {
                    Log.e("WebSocket", "Error parsing message: ${e.localizedMessage}")
                }
            }

            override fun onFailure(webSocket: WebSocket, t: Throwable, response: okhttp3.Response?) {
                Log.e("WebSocket", "Error in WebSocket: ${t.message}")
                reconnect(webSocket, instrumentId)
            }


            override fun onClosed(webSocket: WebSocket, code: Int, reason: String) {
                Log.d("WebSocket", "WebSocket closed: Code=$code Reason=$reason")
            }
        }

        webSocket = client.newWebSocket(request, listener)  // Assign new WebSocket
        awaitClose {
            Log.d("WebSocket", "Closing WebSocket connection.")
            webSocket?.close(1000, "Normal closure")
        }
    }

    private fun reconnect(webSocket: WebSocket, instrumentId: String) {
        Log.d("WebSocket", "Attempting to reconnect...")
        webSocket.close(1000, "Reconnecting")
        connect(instrumentId)
    }
}
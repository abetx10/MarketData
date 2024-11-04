package com.example.marketdata.data.network

import com.example.marketdata.domain.model.TokenResponse
import com.example.marketdata.domain.model.InstrumentResponse
import retrofit2.Response
import retrofit2.http.*

interface ApiService {

    @FormUrlEncoded
    @POST("identity/realms/fintatech/protocol/openid-connect/token")
    suspend fun getToken(
        @Field("grant_type") grantType: String = "password",
        @Field("client_id") clientId: String = "app-cli",
        @Field("username") username: String = "r_test@fintatech.com",
        @Field("password") password: String = "kisfiz-vUnvy9-sopnyv"
    ): Response<TokenResponse>

    @GET("api/instruments/v1/instruments")
    suspend fun getInstruments(
        @Header("Authorization") token: String,
        @Query("provider") provider: String = "simulation",
    ): Response<InstrumentResponse>

    @GET("api/bars/v1/bars/count-back")
    suspend fun getHistoricalData(
        @Header("Authorization") token: String,
        @Query("instrumentId") instrumentId: String,
        @Query("provider") provider: String = "oanda",
        @Query("interval") interval: Int = 1,
        @Query("periodicity") periodicity: String = "day",
        @Query("barsCount") barsCount: Int = 30
    ): Response<InstrumentResponse>
}
package com.example.polarissima.retrofit

import retrofit2.http.GET
import retrofit2.http.Query

interface GeocodingAPI {
    @GET("direct")
    suspend fun getGeocodingData(
        @Query("q") location: String,
        @Query("limit") limit: Int,
        @Query("appid") apiKey: String
    ): List<GeocodingData>
}

data class GeocodingData(
    val lat: Double,
    val lon: Double,
    val name: String,
    val state: String?,
    val country: String
)
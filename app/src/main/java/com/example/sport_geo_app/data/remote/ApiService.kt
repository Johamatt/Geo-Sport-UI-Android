package com.example.sport_geo_app.data.remote
import com.example.sport_geo_app.data.model.SportPlace
import retrofit2.http.GET
import retrofit2.http.Query
interface ApiService {
    @GET("sportPlaces/nearby")
    suspend fun getNearbyPlaces(
        @Query("latitude") latitude: Double,
        @Query("longitude") longitude: Double,
        @Query("radius") radius: Int,
        @Query("page") page: Int,
        @Query("limit") limit: Int
    ): List<SportPlace>
}
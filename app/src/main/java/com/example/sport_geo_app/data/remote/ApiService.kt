package com.example.sport_geo_app.data.remote
import com.example.sport_geo_app.data.model.Place
import retrofit2.http.GET
import retrofit2.http.Query
interface ApiService {
    @GET("places/nearby")
    suspend fun getNearbyPlaces(
        @Query("latitude") latitude: Double,
        @Query("longitude") longitude: Double,
        @Query("radius") radius: Int
    ): List<Place>
}
package com.example.sport_geo_app.data.remote

import com.example.sport_geo_app.data.model.Place

class PlaceRepository {
    companion object {
        suspend fun fetchNearbyPlaces(latitude: Double, longitude: Double, radius: Int): List<Place> {
            return ApiClient.getApiService().getNearbyPlaces(latitude, longitude, radius)
        }
    }
}
package com.example.sport_geo_app.data.model
import com.google.gson.annotations.SerializedName

data class Place(
    val id: Int,
    val name: String,
    val latitude: Double,
    val longitude: Double,
    val type: String,
    @SerializedName("street_address")
    val streetAddress: String,
    val city: String,
    @SerializedName("postal_code")
    val postalCode: String,
    val county: String,
    val country: String,
    val subtype: String,
    @SerializedName("mainType")
    val mainType: String,
    val district: String,
    val createdAt: String,
    val updatedAt: String
)
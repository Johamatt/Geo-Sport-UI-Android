package com.example.sport_geo_app.data.model
import com.google.gson.annotations.SerializedName

data class SportPlace(
    val id: Int,
    val name: String,
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
    val geom: Geom
)

data class Geom(
    val coordinates: List<Double>,
    val type: String,
    val crs: Crs
)

data class Crs(
    val type: String,
    val properties: Properties
)

data class Properties(
    val name: String,
    val type: String
)
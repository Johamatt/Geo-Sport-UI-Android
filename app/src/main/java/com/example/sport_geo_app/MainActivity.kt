package com.example.sport_geo_app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.navigation.compose.rememberNavController
import com.example.sport_geo_app.navigation.AppNavHost
import com.example.sport_geo_app.ui.theme.SportgeoappTheme

import com.example.sport_geo_app.utils.BitmapUtils.bitmapFromDrawableRes
import android.graphics.Color
import androidx.core.content.ContextCompat
import com.example.sport_geo_app.R
import com.mapbox.geojson.Point
import com.mapbox.maps.CameraOptions
import com.mapbox.maps.MapView
import com.mapbox.maps.Style
import com.mapbox.maps.extension.style.expressions.dsl.generated.*
import com.mapbox.maps.extension.style.expressions.generated.Expression.Companion.step
import com.mapbox.maps.extension.style.layers.addLayer
import com.mapbox.maps.extension.style.layers.generated.circleLayer
import com.mapbox.maps.extension.style.layers.generated.symbolLayer
import com.mapbox.maps.extension.style.sources.addSource
import com.mapbox.maps.extension.style.sources.generated.geoJsonSource
import com.mapbox.maps.extension.style.style
import com.mapbox.maps.extension.style.utils.ColorUtils
import com.mapbox.maps.extension.style.utils.transition
import com.mapbox.maps.plugin.animation.flyTo


class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val mapView = MapView(this)
        setContentView(mapView)
        val mapboxMap = mapView.mapboxMap

        mapboxMap.loadStyle(
            styleExtension = style(Style.STANDARD) {
                +transition {
                    duration(0)
                    delay(0)
                    enablePlacementTransitions(false)
                }
            },
            onStyleLoaded = {
                mapboxMap.flyTo(
                    CameraOptions.Builder()
                        .center(Point.fromLngLat(24.857163238, 60.241499034))
                        .zoom(10.0)
                        .pitch(0.0)
                        .build()
                )
                addClusteredGeoJsonSource(it)
                bitmapFromDrawableRes(this, R.drawable.ic_cross)?.let { bitmap ->
                    it.addImage(CROSS_ICON_ID, bitmap, true)
                }
            }
        )
    }

    private fun addClusteredGeoJsonSource(style: Style) {

        // Add a new source from the GeoJSON data and set the 'cluster' option to true.
        style.addSource(
            geoJsonSource(GEOJSON_SOURCE_ID) {
                data("http://10.0.2.2:3000/sportPlaces/map/findAllGeoJson")
                cluster(true)
                maxzoom(14)
                clusterRadius(50)
            }
        )

        // Creating a marker layer for single data points
        style.addLayer(
            symbolLayer("unclustered-points", GEOJSON_SOURCE_ID) {
                iconImage(CROSS_ICON_ID)
                iconSize(
                    literal(1) // Set a fixed icon size or calculate size based on another property if available in your GeoJSON data
                )

                filter(
                    has {
                        literal("name")
                    }
                )
            }
        )

        val layers = arrayOf(
            intArrayOf(150, ContextCompat.getColor(this, R.color.red)),
            intArrayOf(20, ContextCompat.getColor(this, R.color.green)),
            intArrayOf(0, ContextCompat.getColor(this, R.color.blue))
        )

        // Add clusters' circles
        style.addLayer(
            circleLayer("clusters", GEOJSON_SOURCE_ID) {
                circleColor(
                    step(
                        input = get("point_count"),
                        output = literal(ColorUtils.colorToRgbaString(layers[2][1])),
                        stops = arrayOf(
                            literal(layers[1][0].toDouble()) to literal(ColorUtils.colorToRgbaString(layers[1][1])),
                            literal(layers[0][0].toDouble()) to literal(ColorUtils.colorToRgbaString(layers[0][1]))
                        )
                    )
                )
                circleRadius(18.0)
                filter(
                    has("point_count")
                )
            }
        )

        style.addLayer(
            symbolLayer("count", GEOJSON_SOURCE_ID) {
                textField(
                    format {
                        formatSection(
                            toString {
                                get {
                                    literal("point_count")
                                }
                            }
                        )
                    }
                )
                textSize(12.0)
                textColor(Color.WHITE)
                textIgnorePlacement(true)
                textAllowOverlap(true)
            }
        )
    }

    companion object {
        private const val GEOJSON_SOURCE_ID = "sportplaces"
        private const val CROSS_ICON_ID = "cross-icon-id"
    }
}

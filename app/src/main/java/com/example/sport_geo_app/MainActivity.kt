package com.example.sport_geo_app

import android.os.Bundle
import androidx.activity.ComponentActivity
import com.example.sport_geo_app.utils.BitmapUtils.bitmapFromDrawableRes
import android.graphics.Color
import android.widget.TextView
import android.widget.Toast
import androidx.core.content.ContextCompat
import com.example.sport_geo_app.utils.LocationPermissionHelper
import com.mapbox.android.gestures.MoveGestureDetector
import com.mapbox.geojson.Point
import com.mapbox.maps.CameraOptions
import com.mapbox.maps.ImageHolder
import com.mapbox.maps.MapView
import com.mapbox.maps.RenderedQueryGeometry
import com.mapbox.maps.*
import com.mapbox.maps.Style
import com.mapbox.maps.extension.style.expressions.dsl.generated.*
import com.mapbox.maps.extension.style.expressions.generated.Expression.Companion.step
import com.mapbox.maps.extension.style.layers.addLayer
import com.mapbox.maps.extension.style.layers.generated.circleLayer
import com.mapbox.maps.extension.style.layers.generated.symbolLayer
import com.mapbox.maps.extension.style.sources.addSource
import com.mapbox.maps.extension.style.sources.generated.geoJsonSource
import com.mapbox.maps.extension.style.utils.ColorUtils
import com.mapbox.maps.plugin.LocationPuck2D
import com.mapbox.maps.plugin.PuckBearing
import com.mapbox.maps.plugin.animation.MapAnimationOptions
import com.mapbox.maps.plugin.animation.flyTo
import com.mapbox.maps.plugin.gestures.OnMoveListener
import com.mapbox.maps.plugin.gestures.addOnMapClickListener
import com.mapbox.maps.plugin.gestures.gestures
import com.mapbox.maps.plugin.locationcomponent.OnIndicatorBearingChangedListener
import com.mapbox.maps.plugin.locationcomponent.OnIndicatorPositionChangedListener
import com.mapbox.maps.plugin.locationcomponent.location
import com.mapbox.maps.viewannotation.viewAnnotationOptions
import java.lang.ref.WeakReference
import com.mapbox.maps.viewannotation.ViewAnnotationManager;
import com.mapbox.maps.viewannotation.geometry

class MainActivity : ComponentActivity() {

    private lateinit var mapView: MapView
    private lateinit var locationPermissionHelper: LocationPermissionHelper
    private lateinit var viewAnnotationManager: ViewAnnotationManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        mapView = MapView(this)
        setContentView(mapView)
        locationPermissionHelper = LocationPermissionHelper(WeakReference(this))
        locationPermissionHelper.checkPermissions {
            initializeMap()
        }
        viewAnnotationManager = mapView.viewAnnotationManager
    }

    private val onIndicatorBearingChangedListener = OnIndicatorBearingChangedListener {
        mapView.mapboxMap.setCamera(CameraOptions.Builder().bearing(it).build())
    }

    private val onIndicatorPositionChangedListener = OnIndicatorPositionChangedListener {
        mapView.mapboxMap.setCamera(CameraOptions.Builder().center(it).build())
        mapView.gestures.focalPoint = mapView.mapboxMap.pixelForCoordinate(it)
    }

    private val onMoveListener = object : OnMoveListener {
        override fun onMoveBegin(detector: MoveGestureDetector) {
            onCameraTrackingDismissed()
        }

        override fun onMove(detector: MoveGestureDetector): Boolean = false

        override fun onMoveEnd(detector: MoveGestureDetector) {}
    }



    override fun onDestroy() {
        super.onDestroy()
        mapView.location.removeOnIndicatorBearingChangedListener(onIndicatorBearingChangedListener)
        mapView.location.removeOnIndicatorPositionChangedListener(onIndicatorPositionChangedListener)
        mapView.gestures.removeOnMoveListener(onMoveListener)
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        locationPermissionHelper.onRequestPermissionsResult(requestCode, permissions, grantResults)
    }

    private fun initializeMap() {
        mapView.mapboxMap.apply {
            setCamera(CameraOptions.Builder().zoom(10.0).pitch(0.0).build())
            loadStyle(Style.STANDARD) {
                initLocationComponent()
                setupGesturesListener()
                addClusteredGeoJsonSource(it)
                addOnMapClickListener { point ->
                    handleMapClick(point)
                    true
                }

                bitmapFromDrawableRes(this@MainActivity, R.drawable.ice_skating)?.let { bitmap ->
                    it.addImage(ICE_SKATING_ID, bitmap, true)
                }

                bitmapFromDrawableRes(this@MainActivity, R.drawable.ic_pin)?.let { bitmap ->
                    it.addImage(PIN_ID, bitmap, true)
                }

                bitmapFromDrawableRes(this@MainActivity, R.drawable.football)?.let { bitmap ->
                    it.addImage(FOOTBALL_ID, bitmap, true)
                }

                bitmapFromDrawableRes(this@MainActivity, R.drawable.basketball)?.let { bitmap ->
                    it.addImage(BASKETBALL_ID, bitmap, true)
                }

                bitmapFromDrawableRes(this@MainActivity, R.drawable.dog_park)?.let { bitmap ->
                    it.addImage(DOG_PARK_ID, bitmap, true)
                }

                bitmapFromDrawableRes(this@MainActivity, R.drawable.swim)?.let { bitmap ->
                    it.addImage(SWIM_ID, bitmap, true)
                }

                bitmapFromDrawableRes(this@MainActivity, R.drawable.tennis)?.let { bitmap ->
                    it.addImage(TENNIS_ID, bitmap, true)
                }

                bitmapFromDrawableRes(this@MainActivity, R.drawable.gym)?.let { bitmap ->
                    it.addImage(GYM_ID, bitmap, true)
                }

                bitmapFromDrawableRes(this@MainActivity, R.drawable.volleyball)?.let { bitmap ->
                    it.addImage(VOLLEYBALL_ID, bitmap, true)
                }

                bitmapFromDrawableRes(this@MainActivity, R.drawable.golf)?.let { bitmap ->
                    it.addImage(GOLF_ID, bitmap, true)
                }

                bitmapFromDrawableRes(this@MainActivity, R.drawable.boat)?.let { bitmap ->
                    it.addImage(BOAT_ID, bitmap, true)
                }



            }
        }
    }

    private fun initLocationComponent() {
        val locationComponentPlugin = mapView.location
        locationComponentPlugin.updateSettings {
            puckBearing = PuckBearing.COURSE
            puckBearingEnabled = true
            enabled = true
            locationPuck = LocationPuck2D(
                bearingImage = ImageHolder.from(R.drawable.mapbox_user_puck_icon),
                shadowImage = ImageHolder.from(R.drawable.mapbox_user_icon_shadow),
                scaleExpression = interpolate {
                    linear()
                    zoom()
                    stop { literal(0.0); literal(0.6) }
                    stop { literal(20.0); literal(1.0) }
                }.toJson()
            )
        }
        locationComponentPlugin.addOnIndicatorPositionChangedListener(onIndicatorPositionChangedListener)
        locationComponentPlugin.addOnIndicatorBearingChangedListener(onIndicatorBearingChangedListener)
    }

    private fun setupGesturesListener() {
        mapView.gestures.addOnMoveListener(onMoveListener)
    }

    private fun onCameraTrackingDismissed() {
        Toast.makeText(this, "onCameraTrackingDismissed", Toast.LENGTH_SHORT).show()
        mapView.location.removeOnIndicatorPositionChangedListener(onIndicatorPositionChangedListener)
        mapView.location.removeOnIndicatorBearingChangedListener(onIndicatorBearingChangedListener)
        mapView.gestures.removeOnMoveListener(onMoveListener)
    }

    private fun handleMapClick(point: Point) {
        val screenPoint = mapView.mapboxMap.pixelForCoordinate(point)
        viewAnnotationManager.removeAllViewAnnotations()
        mapView.mapboxMap.queryRenderedFeatures(
            RenderedQueryGeometry(screenPoint),
            RenderedQueryOptions(listOf("unclustered-points", "clusters"), null)
        ) { features ->
            features.value?.firstOrNull()?.let { feature ->
                val layer = feature.layers?.getOrNull(0)
                val values = feature.queriedFeature?.feature
                when (layer) {
                    "clusters" -> {
                        if (values != null) {
                            val coordinates = values.geometry() as? Point
                            val currentZoom = mapView.mapboxMap.cameraState.zoom
                            val newZoom = if (currentZoom >= 12.0) currentZoom + 1 else 12.0
                            mapView.mapboxMap.flyTo(
                                CameraOptions.Builder()
                                    .zoom(newZoom)
                                    .pitch(0.0)
                                    .center(coordinates)
                                    .build(),
                                MapAnimationOptions.Builder()
                                    .duration(1000)
                                    .build()
                            )
                        }
                    }
                    "unclustered-points" -> {
                        if (values != null) {
                            val coordinates = values.geometry() as? Point
                            val name = values.getStringProperty("name") ?: ""
                            val category = values.getStringProperty("type") ?: ""
                            val viewAnnotation = viewAnnotationManager.addViewAnnotation(
                                resId = R.layout.point_info_layout,
                                options = viewAnnotationOptions {
                                    if (coordinates != null) {
                                        geometry(coordinates)
                                    }
                                }
                            )
                            viewAnnotation?.findViewById<TextView>(R.id.annotation)?.text = name
                            viewAnnotation?.findViewById<TextView>(R.id.category)?.text = category
                        }
                    }
                }
            }
        }
    }


    private fun addClusteredGeoJsonSource(style: Style) {
        style.addSource(
            geoJsonSource(GEOJSON_SOURCE_ID) {
                data("http://10.0.2.2:3000/sportPlaces/map/findAllGeoJson")

                cluster(true)
                maxzoom(14)
                clusterRadius(50)
            }
        )

        style.addLayer(
            symbolLayer("unclustered-points", GEOJSON_SOURCE_ID) {
                iconAllowOverlap(false)
                iconImage(
                    match {
                        get("type")
                        literal("Pallokenttä")
                        literal(FOOTBALL_ID)
                        literal("Jalkapallohalli")
                        literal(FOOTBALL_ID)
                        literal("Luistelukenttä")
                        literal(ICE_SKATING_ID)
                        literal("Tenniskenttäalue")
                        literal(TENNIS_ID)
                        literal("Koripallokenttä")
                        literal(BASKETBALL_ID)
                        literal("Kuntosali")
                        literal(GYM_ID)
                        literal("Liikuntasali")
                        literal(GYM_ID)
                        literal("Koiraurheilualue")
                        literal(DOG_PARK_ID)
                        literal("Koiraurheiluhalli")
                        literal(DOG_PARK_ID)
                        literal("Uimapaikka")
                        literal(SWIM_ID)
                        literal("Veneilyn palvelupaikka")
                        literal(BOAT_ID)
                        literal("Lentopallokenttä")
                        literal(VOLLEYBALL_ID)
                        literal("Golfkenttä")
                        literal(GOLF_ID)

                        literal(PIN_ID) // def
                    }
                )
                iconSize(literal(1))
            }
        )


        val layers = arrayOf(
            intArrayOf(150, ContextCompat.getColor(this, R.color.red)),
            intArrayOf(20, ContextCompat.getColor(this, R.color.green)),
            intArrayOf(0, ContextCompat.getColor(this, R.color.blue))
        )

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
                filter(has("point_count"))
            }
        )

        style.addLayer(
            symbolLayer("count", GEOJSON_SOURCE_ID) {
                textField(format { formatSection(toString { get { literal("point_count") } }) })
                textSize(12.0)
                textColor(Color.WHITE)
                textIgnorePlacement(true)
                textAllowOverlap(true)
            }
        )
    }

    companion object {
        private const val GEOJSON_SOURCE_ID = "sportplaces"
        private const val PIN_ID = "pin-icon-id"

        private const val ICE_SKATING_ID = "hockey-icon-id"
        private const val FOOTBALL_ID = "football-icon-id"
        private const val BASKETBALL_ID = "basketball-icon-id"
        private const val SWIM_ID = "swim-icon-id"
        private const val DOG_PARK_ID = "dog-park-icon-id"
        private const val TENNIS_ID = "tennis-icon-id"
        private const val GYM_ID = "gym-icon-id"
        private const val BOAT_ID = "boat-icon-id"
        private const val VOLLEYBALL_ID = "volleyball-icon-id"
        private const val GOLF_ID = "golf-icon-id"
    }
}

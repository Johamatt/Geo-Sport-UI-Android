package com.example.sport_geo_app

import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import com.example.sport_geo_app.screens.map.LocationListener
import com.example.sport_geo_app.utils.BitmapUtils
import com.example.sport_geo_app.utils.LocationPermissionHelper
import com.mapbox.geojson.Point
import com.mapbox.maps.CameraOptions
import com.mapbox.maps.MapView
import com.mapbox.maps.RenderedQueryGeometry
import com.mapbox.maps.RenderedQueryOptions
import com.mapbox.maps.Style
import com.mapbox.maps.extension.style.expressions.dsl.generated.format
import com.mapbox.maps.extension.style.expressions.dsl.generated.get
import com.mapbox.maps.extension.style.expressions.dsl.generated.has
import com.mapbox.maps.extension.style.expressions.dsl.generated.literal
import com.mapbox.maps.extension.style.expressions.dsl.generated.match
import com.mapbox.maps.extension.style.expressions.generated.Expression
import com.mapbox.maps.extension.style.layers.addLayer
import com.mapbox.maps.extension.style.layers.generated.circleLayer
import com.mapbox.maps.extension.style.layers.generated.symbolLayer
import com.mapbox.maps.extension.style.sources.addSource
import com.mapbox.maps.extension.style.sources.generated.geoJsonSource
import com.mapbox.maps.extension.style.utils.ColorUtils
import com.mapbox.maps.plugin.animation.MapAnimationOptions
import com.mapbox.maps.plugin.animation.flyTo
import com.mapbox.maps.plugin.gestures.addOnMapClickListener
import com.mapbox.maps.viewannotation.ViewAnnotationManager
import com.mapbox.maps.viewannotation.geometry
import com.mapbox.maps.viewannotation.viewAnnotationOptions
import java.lang.ref.WeakReference

class MapFragment : Fragment() {

    private lateinit var mapView: MapView
    private lateinit var locationListener: LocationListener
    private lateinit var locationPermissionHelper: LocationPermissionHelper
    private lateinit var viewAnnotationManager: ViewAnnotationManager

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val view = inflater.inflate(R.layout.fragment_map, container, false)
        mapView = view.findViewById(R.id.mapView)
        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        locationPermissionHelper = LocationPermissionHelper(WeakReference(requireActivity()))
        locationPermissionHelper.checkPermissions {
            initializeMap()
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        locationPermissionHelper.onRequestPermissionsResult(requestCode, permissions, grantResults)
    }

    override fun onDestroy() {
        super.onDestroy()
        if (::locationListener.isInitialized) {
            locationListener.onDestroy(mapView)
        }
    }

    private fun initializeMap() {
        mapView.mapboxMap.apply {
            setCamera(CameraOptions.Builder().zoom(10.0).pitch(0.0).build())
            loadStyle(Style.STANDARD) {
                locationListener = LocationListener(mapView)
                locationListener.setupGesturesListener(mapView)
                locationListener.initLocationComponent(mapView)
                viewAnnotationManager = mapView.viewAnnotationManager
                addClusteredGeoJsonSource(it)
                addOnMapClickListener { point ->
                    handleMapClick(point)
                    true
                }
                for ((drawableRes, id) in resourcesAndIds) {
                    BitmapUtils.bitmapFromDrawableRes(requireContext(), drawableRes)
                        ?.let { bitmap ->
                            it.addImage(id, bitmap, true)
                        }
                }
            }
        }
    }

    private fun handleMapClick(point: Point) {
        if (!::viewAnnotationManager.isInitialized) {
            return
        }
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
                        typeToIconMap.forEach { (typeName, iconId) ->
                            literal(typeName)
                            literal(iconId)
                        }
                        literal(PIN_ID) // Default icon
                    }
                )
                iconSize(literal(1))
            }
        )


        val layers = arrayOf(
            intArrayOf(150, ContextCompat.getColor(requireContext(), R.color.red)),
            intArrayOf(20, ContextCompat.getColor(requireContext(), R.color.green)),
            intArrayOf(0, ContextCompat.getColor(requireContext(), R.color.blue))
        )


        style.addLayer(
            circleLayer("clusters", GEOJSON_SOURCE_ID) {
                circleColor(
                    Expression.step(
                        input = get("point_count"),
                        output = literal(ColorUtils.colorToRgbaString(layers[2][1])),
                        stops = arrayOf(
                            literal(layers[1][0].toDouble()) to literal(
                                ColorUtils.colorToRgbaString(
                                    layers[1][1]
                                )
                            ),
                            literal(layers[0][0].toDouble()) to literal(
                                ColorUtils.colorToRgbaString(
                                    layers[0][1]
                                )
                            )
                        )
                    )
                )
                circleRadius(18.0)
                filter(has("point_count"))
            }
        )

        style.addLayer(
            symbolLayer("count", GEOJSON_SOURCE_ID) {
                textField(format {
                    formatSection(com.mapbox.maps.extension.style.expressions.dsl.generated.toString {
                        get {
                            literal(
                                "point_count"
                            )
                        }
                    })
                })
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

    private val resourcesAndIds = arrayOf(
        Pair(R.drawable.ice_skating, ICE_SKATING_ID),
        Pair(R.drawable.ic_pin, PIN_ID),
        Pair(R.drawable.football, FOOTBALL_ID),
        Pair(R.drawable.basketball, BASKETBALL_ID),
        Pair(R.drawable.dog_park, DOG_PARK_ID),
        Pair(R.drawable.swim, SWIM_ID),
        Pair(R.drawable.tennis, TENNIS_ID),
        Pair(R.drawable.gym, GYM_ID),
        Pair(R.drawable.volleyball, VOLLEYBALL_ID),
        Pair(R.drawable.golf, GOLF_ID),
        Pair(R.drawable.boat, BOAT_ID)
    )

    val typeToIconMap = mapOf(
        "Pallokenttä" to FOOTBALL_ID,
        "Jalkapallohalli" to FOOTBALL_ID,
        "Luistelukenttä" to ICE_SKATING_ID,
        "Tenniskenttäalue" to TENNIS_ID,
        "Koripallokenttä" to BASKETBALL_ID,
        "Kuntosali" to GYM_ID,
        "Liikuntasali" to GYM_ID,
        "Koiraurheilualue" to DOG_PARK_ID,
        "Koiraurheiluhalli" to DOG_PARK_ID,
        "Uimapaikka" to SWIM_ID,
        "Veneilyn palvelupaikka" to BOAT_ID,
        "Lentopallokenttä" to VOLLEYBALL_ID,
        "Golfkenttä" to GOLF_ID
    )

}
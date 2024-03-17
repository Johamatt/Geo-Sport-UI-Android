package com.example.sport_geo_app.screens
import android.graphics.Bitmap
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.core.content.res.ResourcesCompat
import androidx.core.graphics.drawable.toBitmap
import com.example.sport_geo_app.R
import com.example.sport_geo_app.data.remote.PlaceRepository
import com.mapbox.geojson.Point
import com.mapbox.maps.MapInitOptions
import com.mapbox.maps.MapboxExperimental
import com.mapbox.maps.Style
import com.mapbox.maps.extension.compose.MapboxMap
import com.mapbox.maps.extension.compose.animation.viewport.MapViewportState
import com.mapbox.maps.extension.compose.annotation.generated.PointAnnotation
import androidx.compose.runtime.remember
import com.example.sport_geo_app.data.model.Place
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.withContext
import kotlin.math.log

@OptIn(MapboxExperimental::class)
@Composable
fun MapScreen() {
    val mapViewportState = remember {
        MapViewportState().apply {
            setCameraOptions {
                zoom(10.0)
                center(Point.fromLngLat(24.857163238, 60.241499034))
                pitch(0.0)
                bearing(0.0)
            }
        }
    }

    val placesState = remember { mutableStateOf<List<Place>>(emptyList()) }

    Box(modifier = Modifier.fillMaxSize()) {
        MapboxMap(
            modifier = Modifier.fillMaxSize(),
            mapViewportState = mapViewportState,
            mapInitOptionsFactory = { context ->
                MapInitOptions(
                    context = context,
                    styleUri = Style.STANDARD,
                )
            },
        ) {
            fetchPlacesAndAddMarkers(mapViewportState, placesState)
        }
    }
}

@OptIn(MapboxExperimental::class)
@Composable
fun fetchPlacesAndAddMarkers(
    mapViewportState: MapViewportState,
    placesState: MutableState<List<Place>>
) {
    val coroutineScope = rememberCoroutineScope()
    val context = LocalContext.current

    LaunchedEffect(mapViewportState) {
        val fetchedPlaces = coroutineScope {
            withContext(Dispatchers.IO) {
                PlaceRepository.fetchNearbyPlaces(
                    latitude = mapViewportState.cameraState.center.latitude(),
                    longitude = mapViewportState.cameraState.center.longitude(),
                    radius = 500
                )
            }
        }

        placesState.value = fetchedPlaces
    }

    println("Places State Changed: ${placesState}")

    val places = placesState.value

    places.forEach { place ->
        val drawable = ResourcesCompat.getDrawable(
            context.resources,
            R.drawable.red_marker,
            null
        )
        val bitmap = drawable!!.toBitmap(
            drawable.intrinsicWidth,
            drawable.intrinsicHeight,
            Bitmap.Config.ARGB_8888
        )


        val annotationPoint = Point.fromLngLat(place.geom.coordinates[0], place.geom.coordinates[1])
        PointAnnotation(
            iconImageBitmap = bitmap,
            iconSize = 0.5,
            point = annotationPoint,
        )
    }
}
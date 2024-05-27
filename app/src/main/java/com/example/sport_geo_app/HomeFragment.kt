package com.example.sport_geo_app

import android.Manifest
import android.content.Context
import android.location.Location
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.ImageView
import android.widget.ListView
import android.widget.TextView
import androidx.fragment.app.Fragment
import com.example.sport_geo_app.data.model.SportPlace
import com.example.sport_geo_app.data.remote.ApiClient
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.Locale
import android.widget.AbsListView
import androidx.lifecycle.lifecycleScope
import com.example.sport_geo_app.utils.LocationPermissionHelper
import java.lang.ref.WeakReference

class HomeFragment : Fragment() {
    private lateinit var listView: ListView
    private lateinit var adapter: SportPlaceAdapter
    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private lateinit var errorText: TextView
    private var currentPage = 1
    private val limit = 20
    private var isFetching = false
    private lateinit var locationPermissionHelper: LocationPermissionHelper

    private val errorMessages = mapOf(
        Manifest.permission.ACCESS_FINE_LOCATION to "Location permission is required to show nearby sports places. Please enable location permission in your settings.",
        Manifest.permission.ACCESS_COARSE_LOCATION to "Location permission is required to show nearby sports places. Please enable location permission in your settings.",
        SecurityException::class to "Location permission is required to show nearby sports places. Please enable location permission in your settings."
    )

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        super.onCreate(savedInstanceState)
        locationPermissionHelper = LocationPermissionHelper(WeakReference(requireActivity()))
        val rootView = inflater.inflate(R.layout.fragment_home, container, false)
        initializeViews(rootView)
        setupListView()
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(requireContext())
        checkLocationPermissionAndFetchPlaces()
        return rootView
    }

    private fun initializeViews(rootView: View) {
        listView = rootView.findViewById(R.id.listNearby)
        errorText = rootView.findViewById(R.id.errorText)
        adapter = SportPlaceAdapter(requireContext(), mutableListOf())
        listView.adapter = adapter
    }

    private fun setupListView() {
        listView.setOnScrollListener(object : AbsListView.OnScrollListener {
            override fun onScrollStateChanged(view: AbsListView?, scrollState: Int) {}
            override fun onScroll(view: AbsListView?, firstVisibleItem: Int, visibleItemCount: Int, totalItemCount: Int) {
                if (!isFetching && firstVisibleItem + visibleItemCount == totalItemCount && totalItemCount != 0) {
                    fetchNearbyPlaces()
                }
            }
        })
    }

    private fun checkLocationPermissionAndFetchPlaces() {
        locationPermissionHelper.checkPermissions {
            fetchNearbyPlaces()
        }
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String>, grantResults: IntArray) {
        locationPermissionHelper.onRequestPermissionsResult(requestCode, permissions, grantResults)
    }

    private fun fetchNearbyPlaces() {

        if (!isFetching) {
            isFetching = true
            try {
                fusedLocationClient.lastLocation.addOnSuccessListener { location: Location? ->
                    location?.let {
                        loadNearbyPlaces(it.latitude, it.longitude)
                        isFetching = false
                    } ?: run {
                        showErrorText("Last known location is null")
                        Log.d("HomeFragment", "Last known location is null")
                        isFetching = false
                    }
                }.addOnFailureListener { exception ->
                    showErrorText(exception)
                    Log.e("HomeFragment", "Error getting last known location", exception)
                    isFetching = false
                }
            } catch (e: SecurityException) {
                showErrorText(e)
                Log.e("HomeFragment", "Location permissions are not granted", e)
                isFetching = false
            }
            isFetching = false
        }
    }

    private fun loadNearbyPlaces(latitude: Double, longitude: Double) {
        val radius = 50000
        val lat = 60.192059
        val lon = 24.945831
        // TODO replace with lat long
        viewLifecycleOwner.lifecycleScope.launch(Dispatchers.IO) {
            try {
                val apiService = ApiClient.getApiService()
                val places = apiService.getNearbyPlaces(
                    lat,
                    lon,
                    radius,
                    currentPage,
                    limit
                )


                withContext(Dispatchers.Main) {
                    adapter.addAll(places)
                    adapter.notifyDataSetChanged()
                    showListView()
                    currentPage++
                }
            } catch (e: Exception) {
                e.printStackTrace()
                showErrorText(e)
            }
        }
    }

    private fun showErrorText(errorType: Any) {
        val errorMessage = errorMessages[errorType::class]
        errorText.text = errorMessage ?: "An unknown error occurred."
        errorText.visibility = View.VISIBLE
        listView.visibility = View.GONE
        isFetching = false
    }

    private fun showListView() {
        errorText.visibility = View.GONE
        listView.visibility = View.VISIBLE
    }
}



class SportPlaceAdapter(context: Context, places: MutableList<SportPlace>) : ArrayAdapter<SportPlace>(context, 0, places) {

    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
        var itemView = convertView
        if (itemView == null) {
            itemView = LayoutInflater.from(context).inflate(R.layout.list_item_nearby, parent, false)
        }

        val place = getItem(position)

        val logoImage: ImageView = itemView!!.findViewById(R.id.icon)
        val nameTextView: TextView = itemView.findViewById(R.id.name)
        val distanceTextView: TextView = itemView.findViewById(R.id.distance)

        place?.let {
            nameTextView.text = it.name
            if (it.distance != null) {
                distanceTextView.text = "${it.distance} m"
            } else {
                distanceTextView.text = ""
            }
            val iconResource = getIconResource(it.type)
            logoImage.setImageResource(iconResource)
        }

        return itemView
    }

    private fun getIconResource(type: String): Int {
        return when (type.lowercase(Locale.ROOT)) {
            "pallokenttä", "jalkapallohalli" -> R.drawable.football
            "jääkiekko", "luistelukenttä" -> R.drawable.ice_skating
            "tenniskenttäalue" -> R.drawable.tennis
            "koripallokenttä" -> R.drawable.basketball
            "kuntosali", "liikuntasali" -> R.drawable.gym
            "koiraurheilualue", "koiraurheiluhalli" -> R.drawable.dog_park
            "uimapaikka" -> R.drawable.swim
            "veneilyn palvelupaikka" -> R.drawable.boat
            "lentopallokenttä" -> R.drawable.volleyball
            "golfkenttä" -> R.drawable.golf

            else -> R.drawable.baseline_question_24
        }
    }
}

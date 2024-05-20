package com.example.sport_geo_app

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
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
import androidx.core.app.ActivityCompat
import androidx.fragment.app.Fragment
import com.example.sport_geo_app.data.model.SportPlace
import com.example.sport_geo_app.data.remote.ApiClient
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.Locale
import android.widget.AbsListView

class HomeFragment : Fragment() {

    private lateinit var listView: ListView
    private lateinit var adapter: SportPlaceAdapter
    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private var currentPage = 1
    private val limit = 15

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val rootView = inflater.inflate(R.layout.fragment_home, container, false)
        listView = rootView.findViewById(R.id.listNearby)
        adapter = SportPlaceAdapter(requireContext(), mutableListOf())
        listView.adapter = adapter

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(requireContext())
        fetchNearbyPlaces()

        listView.setOnScrollListener(object : AbsListView.OnScrollListener {
            override fun onScrollStateChanged(view: AbsListView?, scrollState: Int) {}
            override fun onScroll(view: AbsListView?, firstVisibleItem: Int, visibleItemCount: Int, totalItemCount: Int) {
                if (firstVisibleItem + visibleItemCount == totalItemCount && totalItemCount != 0) {
                    fetchNearbyPlaces()
                }
            }
        })
        return rootView
    }

    private fun fetchNearbyPlaces() {
        if (ActivityCompat.checkSelfPermission(
                requireContext(),
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
                requireContext(),
                Manifest.permission.ACCESS_COARSE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            ActivityCompat.requestPermissions(
                requireActivity(),
                arrayOf(Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION),
                LOCATION_PERMISSION_REQUEST_CODE
            )
            return
        }

        fusedLocationClient.lastLocation.addOnSuccessListener { location: Location? ->
            if (location != null) {
                val latitude = location.latitude
                val longitude = location.longitude
                val radius = 100000
                GlobalScope.launch(Dispatchers.IO) {
                    try {
                        val apiService = ApiClient.getApiService()
                        val places = apiService.getNearbyPlaces(latitude, longitude, radius, currentPage, limit)

                        withContext(Dispatchers.Main) {
                            adapter.addAll(places)
                            adapter.notifyDataSetChanged()
                            currentPage++
                        }
                    } catch (e: Exception) {
                        e.printStackTrace()
                        Log.d("Homefragment", "Error fetching nearby places")
                    }
                }
            } else {
                // Handle the case where location is null
                Log.d("Homefragment", "Last known location is null")
            }
        }.addOnFailureListener { exception ->
            // Handle the failure case
            Log.e("Homefragment", "Error getting last known location", exception)
        }
    }

    companion object {
        private const val LOCATION_PERMISSION_REQUEST_CODE = 1
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

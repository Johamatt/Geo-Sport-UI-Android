package com.example.sport_geo_app

import android.content.Context
import android.os.Bundle
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
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.Locale

class HomeFragment : Fragment() {

    private lateinit var listView: ListView
    private lateinit var adapter: ArrayAdapter<SportPlace>

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val rootView = inflater.inflate(R.layout.fragment_home, container, false)
        listView = rootView.findViewById(R.id.listNearby)

        // Initialize adapter
        adapter = ArrayAdapter(requireContext(), R.layout.list_item_nearby)
        listView.adapter = adapter

        // Fetch nearby places
        fetchNearbyPlaces()
        return rootView
    }

    private fun fetchNearbyPlaces() {
        val apiService = ApiClient.getApiService()

        // TODO: Replace latitude and longitude with actual values + scroll pagination
        val latitude = 60.250522
        val longitude = 24.841421
        val radius = 1000 // Radius in meters
        val page = 1
        val limit = 10

        GlobalScope.launch(Dispatchers.IO) {
            try {
                val nearbyPlaces = apiService.getNearbyPlaces(latitude, longitude, radius, page, limit)
                withContext(Dispatchers.Main) {

                    val adapter = SportPlaceAdapter(requireContext(), nearbyPlaces)
                    listView.adapter = adapter
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }
}

class SportPlaceAdapter(context: Context, places: List<SportPlace>) : ArrayAdapter<SportPlace>(context, 0, places) {

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
            distanceTextView.text = it.distance.toString() + " m"
            val iconResource = getIconResource(it.type)
            logoImage.setImageResource(iconResource)

        }

        return itemView
    }
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

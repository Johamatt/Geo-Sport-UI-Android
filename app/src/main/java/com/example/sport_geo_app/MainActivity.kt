package com.example.sport_geo_app

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity


class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val intent = Intent(this, MapActivity::class.java)
        startActivity(intent)
    }
}
package com.example.sport_geo_app.screens

import android.util.Log
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import com.example.sport_geo_app.R
import com.example.sport_geo_app.navigation.AppNavigation
import com.example.sport_geo_app.ui.theme.SportgeoappTheme

@Composable
fun LoginScreen(navController: NavHostController) {
    Surface(
        modifier = Modifier.fillMaxSize(),
        color = Color.Black
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Image(
                painter = painterResource(id = R.drawable.ic_launcher_foreground),
                contentDescription = "Logo",
                modifier = Modifier
                    .size(120.dp)
                    .padding(bottom = 32.dp)
            )

            OutlinedTextField(
                value = "",
                onValueChange = { },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 16.dp),
                label = { Text("Email") },
                leadingIcon = {
                    Icon(
                        imageVector = Icons.Default.Email,
                        contentDescription = "Email Icon"
                    )
                }
            )

            OutlinedTextField(
                value = "",
                onValueChange = { },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 16.dp),
                label = { Text("Password") },
                leadingIcon = {
                    Icon(
                        imageVector = Icons.Default.Lock,
                        contentDescription = "Password Icon"
                    )
                },
                visualTransformation = PasswordVisualTransformation(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password)
            )

            Button(onClick = {
                try {
                    navController.navigate(AppNavigation.NavigationItem.Splash.route)
                } catch (e: Exception) {
                    Log.e("Navigation", "Error navigating to map screen", e)
                }
            }) {
                Text("Login")
            }
        }
    }
}

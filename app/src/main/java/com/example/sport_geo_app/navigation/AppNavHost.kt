package com.example.sport_geo_app.navigation

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.example.sport_geo_app.screens.LoginScreen
import com.example.sport_geo_app.screens.MapScreen
import com.example.sport_geo_app.screens.SplashScreen

@Composable
fun AppNavHost(
    modifier: Modifier = Modifier,
    navController: NavHostController,
    startDestination: String = AppNavigation.NavigationItem.Splash.route,
) {
    NavHost(
        modifier = modifier,
        navController = navController,
        startDestination = startDestination
    ) {
        composable(AppNavigation.NavigationItem.Splash.route) {
            SplashScreen(navController)
        }
        composable(AppNavigation.NavigationItem.Login.route) {
            LoginScreen(navController)
        }
        composable(AppNavigation.NavigationItem.Map.route) {
            MapScreen()
        }
    }
}

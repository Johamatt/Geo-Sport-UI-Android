package com.example.sport_geo_app.navigation

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable

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
        composable(AppNavigation.NavigationItem.Home.route) {
            HomeScreen(navController)
        }

//
    }
}
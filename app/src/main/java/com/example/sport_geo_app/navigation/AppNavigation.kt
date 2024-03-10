package com.example.sport_geo_app.navigation

class AppNavigation {
    enum class Screen {
        SPLASH,
        LOGIN,
        MAP
    }
    sealed class NavigationItem(val route: String) {
        object Splash : NavigationItem(Screen.SPLASH.name)
        object Login : NavigationItem(Screen.LOGIN.name)
        object Map : NavigationItem(Screen.MAP.name)
    }
}
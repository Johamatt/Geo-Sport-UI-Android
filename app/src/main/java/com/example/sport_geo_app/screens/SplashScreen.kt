package com.example.sport_geo_app.screens
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import com.example.sport_geo_app.R
import com.example.sport_geo_app.navigation.AppNavigation

@Composable
fun SplashScreen(navController: NavController) {
    // Animation values
    val alphaValue = animateFloatAsState(
        targetValue = 1f,
        animationSpec = tween(durationMillis = 1500)
    ).value
    navController.navigate(AppNavigation.NavigationItem.Login.route)

    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        /*
        Image(
            painter = painterResource(id = R.drawable.androidparty),
            contentDescription = "Splash Screen",
            modifier = Modifier.alpha(alphaValue)
        )
        */

    }
}

@Preview
@Composable
fun SplashScreenPreview() {
    SplashScreen(navController = rememberNavController())
}


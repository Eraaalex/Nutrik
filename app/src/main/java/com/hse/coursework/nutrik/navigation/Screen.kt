package com.hse.coursework.nutrik.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.Camera
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.SwapVerticalCircle
import androidx.compose.ui.graphics.vector.ImageVector

sealed class Screen(val route: String, val icon: ImageVector? = null) {
    object Auth : Screen("auth")
    object Main : Screen("Main", Icons.Default.Home)
    object Search : Screen("Search", Icons.Default.Search)
    object Favs : Screen("Favs", Icons.Default.Favorite)
    object Camera : Screen("camera_screen", Icons.Default.Camera)
    object ProductDetail : Screen("product_detail_screen/{productId}") {
        fun createRoute(productId: String) = "product_detail_screen/$productId"
    }

    object Account : Screen("account_screen", Icons.Default.AccountCircle)
    object Splash : Screen("splash_screen", Icons.Default.SwapVerticalCircle)
}

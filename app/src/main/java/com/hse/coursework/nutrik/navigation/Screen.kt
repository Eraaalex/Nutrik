package com.hse.coursework.nutrik.navigation

import android.graphics.drawable.Icon
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.Camera
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Search
import androidx.compose.ui.graphics.vector.ImageVector

sealed class Screen(val route: String, val icon: ImageVector? = null) {
    object Auth : Screen("auth")
    object Main : Screen("main_screen", Icons.Default.Home)
    object Search : Screen("search_screen", Icons.Default.Search)
    object Favs : Screen("favs_screen", Icons.Default.Favorite)
    object Camera : Screen("camera_screen", Icons.Default.Camera)
    object ProductDetail : Screen("product_detail_screen/{productId}") {
        fun createRoute(productId: String) = "product_detail_screen/$productId"
    }

    object Account : Screen("account_screen", Icons.Default.AccountCircle)
}

package com.hse.coursework.nutrik.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.hse.coursework.nutrik.auth.AuthScreen
import com.hse.coursework.nutrik.screen.favourite.FavouriteScreen
import com.hse.coursework.nutrik.screen.main.MainScreen
import com.hse.coursework.nutrik.screen.search.SearchScreen
import com.hse.coursework.nutrik.screen.product.ProductDetailScreen

@Composable
fun NavigationGraph(
    navController: NavHostController,
    isUserAuthenticated: Boolean
) {
    val startDestination = if (isUserAuthenticated) {
        Screen.Main.route
    } else {
        Screen.Auth.route
    }
    NavHost(
        navController = navController,
        startDestination = startDestination
    ) {
        composable(Screen.Auth.route) {
            AuthScreen(
                onAuthSuccess = {
                    navController.navigate(Screen.Main.route) {
                        popUpTo(Screen.Auth.route) { inclusive = true }
                    }
                }
            )
        }

        composable(Screen.Main.route) {
            MainScreen(navController)
        }
        composable(Screen.Search.route) {
            SearchScreen(navController)
        }
        composable(Screen.Favs.route) {
            FavouriteScreen(navController)
        }
        composable(Screen.Camera.route) {
            CameraScreen(navController)
        }
        composable(
            route = Screen.ProductDetail.route,
            arguments = listOf(navArgument("productId") { type = NavType.StringType })
        ) { backStackEntry ->
            val productId = backStackEntry.arguments?.getString("productId") ?: ""
            ProductDetailScreen(navController, productId)
        }
    }
}

@Composable
fun CameraScreen(navController: NavHostController) {
 // TODO Implement camera screen
}




package com.hse.coursework.nutrik


import android.annotation.SuppressLint
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.hse.coursework.nutrik.navigation.Screen
import com.hse.coursework.nutrik.ui.theme.screen.account.AccountScreen
import com.hse.coursework.nutrik.ui.theme.screen.auth.AuthScreen
import com.hse.coursework.nutrik.ui.theme.screen.auth.AuthViewModel
import com.hse.coursework.nutrik.ui.theme.screen.camera.CameraScreen
import com.hse.coursework.nutrik.ui.theme.screen.favourite.FavouriteScreen
import com.hse.coursework.nutrik.ui.theme.screen.main.MainScreen
import com.hse.coursework.nutrik.ui.theme.screen.product.ProductDetailScreen
import com.hse.coursework.nutrik.ui.theme.screen.search.SearchScreen
import com.hse.coursework.nutrik.utils.SplashScreen

@SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
@Composable
fun NutrikApp(authViewModel: AuthViewModel = hiltViewModel()) {
    val navController = rememberNavController()
    val isUserAuthenticated by authViewModel.isUserAuthenticated.collectAsState()
    LaunchedEffect(isUserAuthenticated) {
        if (isUserAuthenticated) {
            navController.navigate(Screen.Main.route) {
                popUpTo(Screen.Auth.route) { inclusive = true }
            }
        } else {
            navController.navigate(Screen.Auth.route) {
                popUpTo(Screen.Main.route) { inclusive = true }
            }
        }
    }

    NavHost(
        navController = navController,
        startDestination = if (isUserAuthenticated) Screen.Main.route else Screen.Auth.route
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
        composable(Screen.Account.route) {
            AccountScreen(navController)
        }
        composable(
            route = Screen.ProductDetail.route,
            arguments = listOf(navArgument("productId") {
                type = NavType.StringType
            })
        ) { backStackEntry ->
            val productId = backStackEntry.arguments?.getString("productId").orEmpty()
            ProductDetailScreen(navController, productId)
        }
    }

}

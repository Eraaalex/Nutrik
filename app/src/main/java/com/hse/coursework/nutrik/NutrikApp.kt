package com.hse.coursework.nutrik


import android.annotation.SuppressLint
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.hse.coursework.nutrik.auth.AuthScreen
import com.hse.coursework.nutrik.auth.AuthViewModel
import com.hse.coursework.nutrik.navigation.NavigationGraph
import com.hse.coursework.nutrik.ui.theme.NutrikTheme
import com.hse.coursework.nutrik.ui.theme.components.BottomNavigationBar
import dagger.hilt.android.HiltAndroidApp

@SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
@Composable
fun NutrikApp(authViewModel: AuthViewModel = hiltViewModel()) {
    val navController = rememberNavController()
    val isUserAuthenticated by authViewModel.isUserAuthenticated.collectAsState()

    NavigationGraph(
        navController = navController,
        isUserAuthenticated = isUserAuthenticated
    )

}


//// A surface container using the 'background' color from the theme
//Surface(
//modifier = Modifier.fillMaxSize(),
//color = MaterialTheme.colorScheme.background
//) {
//    Greeting("Android")
//}
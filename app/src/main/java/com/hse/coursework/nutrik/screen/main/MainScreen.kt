package com.hse.coursework.nutrik.screen.main

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material.Button
import androidx.compose.material.Icon
import androidx.compose.material.IconButton
import androidx.compose.material.TopAppBar
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import com.hse.coursework.nutrik.navigation.Screen
import com.hse.coursework.nutrik.ui.theme.components.BottomNavigationBar
import com.hse.coursework.nutrik.ui.theme.components.ProgressBarCard

@Composable
fun MainScreen(navController: NavHostController,
               mainViewModel: MainViewModel = hiltViewModel()) {
    val progressData by mainViewModel.progressData.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(text = "Главный") },
                backgroundColor = MaterialTheme.colorScheme.primary,
                contentColor = Color.White,
                actions = {
                    IconButton(onClick = { navController.navigate(Screen.Account.route) }) {
                        Icon(
                            imageVector = Screen.Account.icon!!,
                            contentDescription = "Профиль",
                            tint = MaterialTheme.colorScheme.secondary
                        )
                    }
                }
            )
        },
        bottomBar = {
            BottomNavigationBar(navController = navController)
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            Button(
                onClick = { navController.navigate(Screen.Camera.route) },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
            ) {
                Text(text = "Кнопка для открытия камеры сканера")
            }

            LazyVerticalGrid(
                columns = GridCells.Fixed(2),
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 16.dp)
            ) {
                items(progressData) { item ->
                    ProgressBarCard(
                        title = item.title,
                        progress = item.progress
                    )
                }
            }
        }
    }
}

package com.hse.coursework.nutrik.ui.theme.screen.main

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.Icon
import androidx.compose.material.IconButton
import androidx.compose.material.TopAppBar
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import com.hse.coursework.nutrik.navigation.Screen
import com.hse.coursework.nutrik.ui.theme.components.BottomNavigationBar
import com.hse.coursework.nutrik.ui.theme.components.main.EmptyNeuroAdviceCard
import com.hse.coursework.nutrik.ui.theme.components.main.NeuroAdviceCard
import com.hse.coursework.nutrik.ui.theme.components.main.ProgressContent
import com.hse.coursework.nutrik.ui.theme.components.main.ScanButton

@Composable
fun MainScreen(
    navController: NavHostController,
    mainViewModel: ProgressViewModel = hiltViewModel()
) {

    val uiState by mainViewModel.uiState.collectAsState()
    val neuroTip by mainViewModel.neuroAdvice.collectAsState()
    val weeklyConsumption by mainViewModel.weeklyConsumption.collectAsState()


    LaunchedEffect(Unit) {
        mainViewModel.fetchInitialWeekDataAndTip()
    }


    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(text = "Главный", color = Color(0xFF3D2C1E)) },
                backgroundColor = Color(0xFFFDF6C8),
                contentColor = Color.White,
                actions = {
                    IconButton(onClick = { navController.navigate(Screen.Account.route) }) {
                        Icon(
                            imageVector = Screen.Account.icon!!,
                            contentDescription = "Профиль",
                            tint = Color(0xFF4E2215),
                        )
                    }
                }
            )
        },
        bottomBar = {
            BottomNavigationBar(navController = navController)
        }
    ) { innerPadding ->
        val scrollState = rememberScrollState()
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .verticalScroll(scrollState)
                .background(Color(0xFFfffcdf))
        ) {
            when {
                neuroTip == "Загрузка совета..." -> {
                    EmptyNeuroAdviceCard()
                }

                !neuroTip.isNullOrBlank() -> {
                    NeuroAdviceCard(adviceText = neuroTip!!)
                }
            }
            ScanButton {
                navController.navigate(Screen.Camera.route)
            }

            ProgressContent(
                uiState = uiState,
                onSelectDate = mainViewModel::onSelectDate,
                weeklyConsumption = weeklyConsumption
            )
        }

    }
}

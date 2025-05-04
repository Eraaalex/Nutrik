package com.hse.coursework.nutrik.ui.theme.screen.search

import android.util.Log
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.CircularProgressIndicator
import androidx.compose.material.Icon
import androidx.compose.material.TextField
import androidx.compose.material.TextFieldDefaults
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import com.hse.coursework.nutrik.ui.theme.components.BottomNavigationBar
import com.hse.coursework.nutrik.ui.theme.components.product.ShortProductCard
import kotlinx.coroutines.flow.distinctUntilChanged

@Composable
fun SearchScreen(
    navController: NavHostController,
    viewModel: SearchViewModel = hiltViewModel()
) {
    val searchQuery by viewModel.searchQuery.collectAsState()
    val productList by viewModel.productList.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()

    val listState = rememberLazyListState()
    LaunchedEffect(Unit) {
        viewModel.loadProducts(reset = true)
        viewModel.fetchUserRestrictions()
    }

    LaunchedEffect(listState) {
        snapshotFlow { listState.layoutInfo.visibleItemsInfo.lastOrNull()?.index }
            .distinctUntilChanged()
            .collect { idx ->
                Log.e("SearchScreen", " searchQuery: |$searchQuery|")
                if (idx != null && idx >= productList.size - 1 && searchQuery.isBlank()) {
                    viewModel.loadNextPage()
                }
            }
    }

    LaunchedEffect(searchQuery) {
        if (searchQuery.isBlank()) {
            Log.e("SearchScreen", "productList: $productList")
            viewModel.loadProducts(reset = true)
        }
    }


    Scaffold(
        bottomBar = { BottomNavigationBar(navController) }
    ) { innerPadding ->
        Column(
            Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .background(Color(0xFFfffcdf))
        ) {
            TextField(
                value = searchQuery,
                onValueChange = viewModel::updateSearchQuery,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(8.dp)
                    .border(
                        width = 4.dp,
                        color = Color(0xFFFAF1C3),
                        shape = RoundedCornerShape(12.dp)
                    )
                    .background(Color.Transparent, shape = RoundedCornerShape(12.dp))
                    .height(56.dp),
                textStyle = TextStyle(
                    color = Color(0xFF8A3014),
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold
                ),
                leadingIcon = {
                    Icon(
                        imageVector = Icons.Default.Search,
                        contentDescription = "Поиск",
                        tint = Color(0xFF8A3014)
                    )
                },
                placeholder = {
                    Text(
                        text = "Поиск продуктов",
                        color = Color(0xFF8A3014).copy(alpha = 0.3f),
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold
                    )
                },
                singleLine = true,
                colors = TextFieldDefaults.textFieldColors(
                    backgroundColor = Color.Transparent,
                    focusedIndicatorColor = Color.Transparent,
                    unfocusedIndicatorColor = Color.Transparent,
                    disabledIndicatorColor = Color.Transparent,
                    cursorColor = Color(0xFF8A3014)
                ),
                shape = RoundedCornerShape(12.dp),
                keyboardOptions = KeyboardOptions.Default.copy(
                    keyboardType = KeyboardType.Text
                )
            )


            LazyColumn(
                state = listState,
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(8.dp)
            ) {
                items(productList) { product ->
                    ShortProductCard(
                        product = product,
                        isForbidden = viewModel.isForbidden(product),
                        navController = navController
                    ) {
                        viewModel.clearSearchResults()
                    }
                }

                // индикатор внизу
                if (isLoading) {
                    item {
                        Box(
                            Modifier
                                .fillMaxWidth()
                                .padding(vertical = 16.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            CircularProgressIndicator(color = Color(0xFF8A3014))
                        }
                    }
                }
            }
        }
    }
}


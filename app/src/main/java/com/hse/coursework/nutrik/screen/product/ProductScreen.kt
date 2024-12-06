package com.hse.coursework.nutrik.screen.product

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.hse.coursework.nutrik.model.Product
import com.hse.coursework.nutrik.model.ProductState
import com.hse.coursework.nutrik.ui.theme.components.BottomNavigationBar

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProductDetailScreen(
    navHostController: NavController,
    productId: String,
    viewModel: ProductDetailViewModel = hiltViewModel()
) {
    val productState by viewModel.productState.collectAsState()
    val isFavorite by viewModel.isFavorite.collectAsState()

    LaunchedEffect(productId) {
        viewModel.loadProductById(productId)
    }

    when (productState) {
        is ProductState.Loading -> {
            CircularProgressIndicator()
        }

        is ProductState.Success -> {
            val product = (productState as ProductState.Success).product
            var newWeight by remember { mutableStateOf(product.weight) }

            Scaffold(
                topBar = {
                    TopAppBar(
                        title = {
                            Text(
                                text = "${product.name}, ${product.weight} ${product.unit}",
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        },
                        actions = {
                            IconButton(
                                onClick = { viewModel.onFavoriteClick(product) }
                            ) {
                                Icon(
                                    imageVector = if (isFavorite) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                                    contentDescription = "Favorite",
                                    tint = Color.Gray
                                )
                            }
                        },
                        navigationIcon = {
                            IconButton(onClick = {
                                viewModel.updateConsumption(product, newWeight)
                                navHostController.popBackStack()
                            }) {
                                Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                            }
                        }
                    )
                },
                bottomBar = {
                    BottomBar(product = product, onWeightChange = { weight ->
                        newWeight = weight
                    })
                }
            ) { innerPadding ->
                ProductDetailContent(
                    product = product,
                    modifier = Modifier.padding(innerPadding)
                )
            }
        }

        is ProductState.Error -> {
            Scaffold(
                bottomBar = { BottomNavigationBar(navController = navHostController) }) { innerPadding ->
                Text(
                    text = "Error: ${(productState as ProductState.Error).message}",
                    modifier = Modifier.padding(innerPadding)
                )
            }
        }
    }
}


@Composable
fun ProductDetailContent(product: Product, modifier: Modifier = Modifier) {
    var isCompositionExpanded by remember { mutableStateOf(false) }

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Text(
            text = product.description,
            fontSize = 14.sp,
            color = Color.Gray,
            modifier = Modifier.padding(bottom = 16.dp)
        )

        // КБЖУ
        Text(
            text = "В 100 граммах",
            fontSize = 16.sp,
            fontWeight = FontWeight.SemiBold,
            modifier = Modifier.padding(bottom = 8.dp)
        )
        Row(
            horizontalArrangement = Arrangement.SpaceBetween,
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(text = "${product.energyValue}", fontWeight = FontWeight.Bold)
                Text(text = "Ккал", color = Color.Gray)
            }
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(text = "${product.proteins}", fontWeight = FontWeight.Bold)
                Text(text = "Белки", color = Color.Gray)
            }
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(text = "${product.fats}", fontWeight = FontWeight.Bold)
                Text(text = "Жиры", color = Color.Gray)
            }
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(text = "${product.carbs}", fontWeight = FontWeight.Bold)
                Text(text = "Углеводы", color = Color.Gray)
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        Column(
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { isCompositionExpanded = !isCompositionExpanded }
            ) {
                Text(
                    text = "Состав",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.SemiBold,
                    modifier = Modifier.weight(1f)
                )
                Icon(
                    imageVector = if (isCompositionExpanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                    contentDescription = "Toggle Composition",
                    tint = Color.Gray
                )
            }
            if (isCompositionExpanded) {
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = product.composition.joinToString(", "),
                    fontSize = 14.sp,
                    color = Color.Gray
                )
            }
        }
    }
}

@Composable
fun BottomBar(product: Product, onWeightChange: (Double) -> Unit) {
    var weight by remember { mutableStateOf(product.weight) }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color(0xFFFF5139))
            .padding(vertical = 12.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween,
            modifier = Modifier.fillMaxWidth()
        ) {
            IconButton(
                onClick = { if (weight > 0) weight -= 10; onWeightChange(weight) }
            ) {
                Icon(
                    imageVector = Icons.Default.Remove,
                    contentDescription = "Decrease weight",
                    tint = Color.White
                )
            }

            Text(
                text = "$weight г",
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White
            )

            IconButton(
                onClick = { weight += 10; onWeightChange(weight) }
            ) {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = "Increase weight",
                    tint = Color.White
                )
            }
        }
    }
}



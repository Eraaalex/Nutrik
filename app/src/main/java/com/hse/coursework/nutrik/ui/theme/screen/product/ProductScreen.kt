package com.hse.coursework.nutrik.ui.theme.screen.product

import android.util.Log
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.DrawerDefaults.shape
import androidx.compose.material.TopAppBar
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.hse.coursework.nutrik.R
import com.hse.coursework.nutrik.model.Product
import com.hse.coursework.nutrik.model.ProductEntity
import com.hse.coursework.nutrik.model.ProductState
import com.hse.coursework.nutrik.model.Restriction
import com.hse.coursework.nutrik.model.toEntity
import com.hse.coursework.nutrik.model.toUI
import com.hse.coursework.nutrik.ui.theme.components.BottomNavigationBar

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
        viewModel.fetchUserRestrictions()
    }

    when (productState) {
        is ProductState.Loading -> {
            CircularProgressIndicator()
        }

        is ProductState.Success -> {
            val product = (productState as ProductState.Success).product
            var newWeight by remember { mutableStateOf(product.weight) }

            Scaffold(
                containerColor = Color(0xFFFDF6C8),
                topBar = {
                    TopAppBar(
                        backgroundColor = Color(0xFFFDF6C8),
                        title = {
                            Text(
                                text = "${product.name}",
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis,
                                color = Color(0xFF3D2C1E)
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
                                navHostController.popBackStack()
                            }) {
                                Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                            }
                        }
                    )
                },
            ) { innerPadding ->
                ProductDetailContent(
                    product = product.toUI(),
                    modifier = Modifier
                        .padding(innerPadding)
                        .background(Color(0xFFfffcdf)),
                    restrictions = viewModel.getUserRestrictions(),
                ) { product, weight ->
                    viewModel.updateConsumption(product, weight)
                }
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
fun ProductDetailContent(
    product: Product,
    modifier: Modifier = Modifier,
    restrictions: List<Restriction> = emptyList(),
    onUpdateConsumption: (ProductEntity, Double) -> Unit
) {
    var isCompositionExpanded by remember { mutableStateOf(false) }
    var weight by remember { mutableStateOf(product.weight) }
    var state = rememberScrollState()

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp)
            .background(Color(0xFFfffcdf))
            .verticalScroll(state)
    ) {

        val imageName = product.imageLinks.lastOrNull() ?: "abstract_product"
        Log.e("ProductDetailContent", "Image name: $imageName")
        val context = LocalContext.current

        var imageResId = imageName?.let {
            context.resources.getIdentifier(it, "drawable", context.packageName)
        }

        Box(
            modifier = Modifier.fillMaxWidth(),
            contentAlignment = Alignment.Center
        ) {
            if (imageResId != null && imageResId != 0) {
                Image(
                    painter = painterResource(id = imageResId),
                    contentDescription = product.name,
                    modifier = Modifier
                        .height(200.dp)
                        .clip(RoundedCornerShape(12.dp)),
                    contentScale = ContentScale.Crop
                )
            }

        }


        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = product.name,
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(bottom = 8.dp),
            color = Color(0xFF6e4628)
        )
        if (product.description.isNotEmpty()) {
            Text(
                text = "Описание",
                fontSize = 16.sp,
                fontWeight = FontWeight.SemiBold,
                modifier = Modifier.padding(bottom = 8.dp),
                color = Color(0xFF3D2C1E)
            )
            Text(
                text = product.description,
                fontSize = 16.sp,
                color = Color.Gray,
                modifier = Modifier.padding(bottom = 16.dp)
            )
        }

        Text(
            text = "В 100 граммах",
            fontSize = 16.sp,
            fontWeight = FontWeight.SemiBold,
            modifier = Modifier.padding(bottom = 8.dp),
            color = Color(0xFF3D2C1E)
        )

        Column(
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Row(
                horizontalArrangement = Arrangement.spacedBy(4.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                NutrientBox(
                    "${product.energyValue} ккал",
                    "Калории",
                    Color(0xFFFFF5D1),
                    Modifier.weight(1f)
                )
                NutrientBox(
                    "${product.proteins} г",
                    "Белки",
                    Color(0xFFF2F8DE),
                    Modifier.weight(1f)
                )
                NutrientBox("${product.fats} г", "Жиры", Color(0xFFFFF5D4), Modifier.weight(1f))
                NutrientBox(
                    "${product.carbs} г",
                    "Углеводы",
                    Color(0xFFFFF5D1),
                    Modifier.weight(1f)
                )
            }
            Spacer(modifier = Modifier.height(8.dp))
            Row(
                horizontalArrangement = Arrangement.Center,
                modifier = Modifier.fillMaxWidth()
            ) {
                NutrientBox(
                    text = if (product.sugar == -1.0) "Нет данных" else "${product.sugar} г",
                    label = "Сахар",
                    color = Color(0xFFF2F8DE),
                    modifier = Modifier.width(150.dp)
                )
                Spacer(modifier = Modifier.width(12.dp))
                NutrientBox(
                    text = if (product.salt == -1.0) "Нет данных" else "${product.salt} г",
                    label = "Соль",
                    color = Color(0xFFFFF5D4),
                    modifier = Modifier.width(150.dp)
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        Log.e(
            "ProductDetailContent",
            "User Restrictions: $restrictions"
        )
        if (restrictions.isNotEmpty() &&
            !(restrictions[0] == Restriction.NONE && restrictions.size == 1)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color(0xFFFFEBDE), shape = RoundedCornerShape(12.dp))
                    .padding(12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    painter = painterResource(id = R.drawable.grief),
                    contentDescription = "Warning",
                    tint = Color(0xFFf2926f)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Не подходит: " + restrictions
                        .filter { it != Restriction.NONE }
                        .joinToString(", ") { it.russianName },
                    color = Color(0xFF7A4E3B)
                )

            }
        } else {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color(0xFFF4FADF), shape = RoundedCornerShape(12.dp))
                    .padding(12.dp)
            ) {
                Icon(
                    painter = painterResource(id = R.drawable.smile),
                    contentDescription = "Warning",
                    tint = Color(0xFF4C6A2B)
                )
                Text("Подходит по всем параметрам", color = Color(0xFF7A4E3B))
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        Column(modifier = Modifier.fillMaxWidth()) {
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
                    fontSize = 16.sp,
                    color = Color(0xFF3D2C1E),
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Color(0xFFfffff0), shape = RoundedCornerShape(8.dp))

                )

            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        var input by remember { mutableStateOf("${product.weight}") }

        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.fillMaxWidth()
        ) {
            BasicTextField(
                value = input,
                onValueChange = { newValue ->
                    val sanitized = newValue.replace(',', '.')
                    if (sanitized.matches(Regex("^\\d*(\\.\\d*)?\$"))) {
                        input = sanitized
                        weight = sanitized.toDoubleOrNull() ?: 0.0
                    }
                },
                modifier = Modifier
                    .weight(1f)
                    .height(56.dp)
                    .border(
                        width = 4.dp,
                        color = Color(0xFFFAF1C3),
                        shape = RoundedCornerShape(12.dp)
                    )
                    .background(color = Color.Transparent, shape = shape)
                    .padding(horizontal = 16.dp),
                textStyle = TextStyle(
                    color = Color(0xFF8A3014),
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold
                ),
                keyboardOptions = KeyboardOptions.Default.copy(
                    keyboardType = KeyboardType.Number
                ),
                singleLine = true,
                decorationBox = { innerTextField ->
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.CenterStart
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Box(modifier = Modifier.weight(1f)) {
                                if (input.isBlank()) {
                                    Text(
                                        "${product.weight}",
                                        color = Color(0xFF8A3014).copy(alpha = 0.3f),
                                        fontSize = 18.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                                innerTextField()
                            }
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("г", color = Color(0xFF8A3014), fontSize = 18.sp)
                        }
                    }
                }
            )

            Spacer(modifier = Modifier.width(8.dp))
            Button(
                onClick = {
                    Log.e("ProductDetailContent", "Button clicked with weight: $weight")
                    onUpdateConsumption(product.toEntity(), weight)
                },
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFD0E4A8)),
                modifier = Modifier.height(56.dp)
            ) {

                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.Add,
                        contentDescription = "Добавить",
                        tint = Color(0xFF566D36)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "в рацион",
                        color = Color(0xFF566D36),
                        fontSize = 18.sp
                    )
                }

            }


        }


    }
}

@Composable
fun NutrientBox(text: String, label: String, color: Color, modifier: Modifier = Modifier) {
    Column(
        modifier = modifier
            .padding(4.dp)
            .background(color, shape = RoundedCornerShape(8.dp))
            .padding(12.dp)
            .height(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(text = text, fontWeight = FontWeight.Bold)
        Text(text = label, fontSize = 12.sp)
    }
}


@Preview
@Composable
fun NutrientBoxPreview() {
    NutrientBox("12 г", "Белки", Color(0xFFF2F8DE))
}
//@Composable
//fun ProductDetailContent(product: ProductEntity, modifier: Modifier = Modifier) {
//    var isCompositionExpanded by remember { mutableStateOf(false) }
//
//    Column(
//        modifier = modifier
//            .fillMaxSize()
//            .padding(16.dp)
//    ) {
//        Text(
//            text = product.description,
//            fontSize = 14.sp,
//            color = Color.Gray,
//            modifier = Modifier.padding(bottom = 16.dp)
//        )
//
//        // КБЖУ
//        Text(
//            text = "В 100 граммах",
//            fontSize = 16.sp,
//            fontWeight = FontWeight.SemiBold,
//            modifier = Modifier.padding(bottom = 8.dp)
//        )
//        Row(
//            horizontalArrangement = Arrangement.SpaceBetween,
//            modifier = Modifier.fillMaxWidth()
//        ) {
//            Column(horizontalAlignment = Alignment.CenterHorizontally) {
//                Text(text = "${product.energyValue}", fontWeight = FontWeight.Bold)
//                Text(text = "Ккал", color = Color.Gray)
//            }
//            Column(horizontalAlignment = Alignment.CenterHorizontally) {
//                Text(text = "${product.proteins}", fontWeight = FontWeight.Bold)
//                Text(text = "Белки", color = Color.Gray)
//            }
//            Column(horizontalAlignment = Alignment.CenterHorizontally) {
//                Text(text = "${product.fats}", fontWeight = FontWeight.Bold)
//                Text(text = "Жиры", color = Color.Gray)
//            }
//            Column(horizontalAlignment = Alignment.CenterHorizontally) {
//                Text(text = "${product.carbs}", fontWeight = FontWeight.Bold)
//                Text(text = "Углеводы", color = Color.Gray)
//            }
//        }
//
//        Spacer(modifier = Modifier.height(16.dp))
//
//        Column(
//            modifier = Modifier.fillMaxWidth()
//        ) {
//            Row(
//                verticalAlignment = Alignment.CenterVertically,
//                modifier = Modifier
//                    .fillMaxWidth()
//                    .clickable { isCompositionExpanded = !isCompositionExpanded }
//            ) {
//                Text(
//                    text = "Состав",
//                    fontSize = 16.sp,
//                    fontWeight = FontWeight.SemiBold,
//                    modifier = Modifier.weight(1f)
//                )
//                Icon(
//                    imageVector = if (isCompositionExpanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
//                    contentDescription = "Toggle Composition",
//                    tint = Color.Gray
//                )
//            }
//            if (isCompositionExpanded) {
//                Spacer(modifier = Modifier.height(8.dp))
//                Text(
//                    text = product.composition.joinToString(", "),
//                    fontSize = 14.sp,
//                    color = Color.Gray
//                )
//            }
//        }
//    }
//}
//
//@Composable
//fun BottomBar(product: ProductEntity, onWeightChange: (Double) -> Unit) {
//    var weight by remember { mutableStateOf(product.weight) }
//
//    Box(
//        modifier = Modifier
//            .fillMaxWidth()
//            .background(Color(0xFFFF5139))
//            .padding(vertical = 12.dp)
//    ) {
//        Row(
//            verticalAlignment = Alignment.CenterVertically,
//            horizontalArrangement = Arrangement.SpaceBetween,
//            modifier = Modifier.fillMaxWidth()
//        ) {
//            IconButton(
//                onClick = { if (weight > 0) weight -= 10; onWeightChange(weight) }
//            ) {
//                Icon(
//                    imageVector = Icons.Default.Remove,
//                    contentDescription = "Decrease weight",
//                    tint = Color.White
//                )
//            }
//
//            Text(
//                text = "$weight г",
//                fontSize = 16.sp,
//                fontWeight = FontWeight.Bold,
//                color = Color.White
//            )
//
//            IconButton(
//                onClick = { weight += 10; onWeightChange(weight) }
//            ) {
//                Icon(
//                    imageVector = Icons.Default.Add,
//                    contentDescription = "Increase weight",
//                    tint = Color.White
//                )
//            }
//        }
//    }
//}



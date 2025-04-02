package com.hse.coursework.nutrik.ui.theme.components.product

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.hse.coursework.nutrik.model.ProductEntity


@Composable
fun ProductDetailCard(
    product: ProductEntity,
    onWeightChange: (Double) -> Unit
) {
    var weight by remember { mutableStateOf(product.weight) }
    var isCompositionExpanded by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp)
    ) {
        Text(
            text = "${product.name}, ${weight} г",
            fontSize = 20.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(bottom = 8.dp)
        )

        Text(
            text = product.description ?: "Описание отсутствует",
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

        // Состав
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
                    imageVector = if (isCompositionExpanded) Icons.Default.ExpandLess
                    else Icons.Default.ExpandMore,
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

        Spacer(modifier = Modifier.height(16.dp))

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(Color(0xFFFF5139))
                .padding(vertical = 8.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth()
            ) {

                Text(
                    text = "-",
                    fontSize = 24.sp,
                    color = Color.White,
                    modifier = Modifier
                        .padding(start = 32.dp)
                        .clickable {
                            if (weight > 0) weight -= 10; onWeightChange(weight)
                        })


                Text(
                    text = "$weight г",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )


                Text(text = "+", fontSize = 24.sp, color = Color.White,
                    modifier = Modifier
                        .padding(end = 32.dp)
                        .clickable { weight += 10; onWeightChange(weight) })

            }
        }

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(color = Color(0xFF43bf5f))
                .padding(vertical = 8.dp, horizontal = 16.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth()
            ) {
                Button(
                    onClick = { if (weight > 0) weight -= 10; onWeightChange(weight) },
                    enabled = weight > 0,
                    modifier = Modifier.size(48.dp)
                ) {
                    Text(text = "-", fontSize = 20.sp)
                }

                Text(
                    text = "$weight ${product.unit}",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(horizontal = 16.dp)
                )

                Button(
                    onClick = { weight += 10; onWeightChange(weight) },
                    modifier = Modifier.size(48.dp)
                ) {
                    Text(text = "+", fontSize = 20.sp)
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun ProductDetailCardPreview() {
    val mockProduct = ProductEntity(
        code = "1",
        name = "Молочный шоколад",
        energyValue = 570.0,
        proteins = 8.0,
        fats = 38.0,
        carbs = 49.0,
        weight = 100.0,
        composition = listOf("Сахар", "Молоко", "Масло какао", "Лецитин")
    )

    MaterialTheme {
        ProductDetailCard(
            product = mockProduct,
            onWeightChange = { newWeight -> }
        )
    }
}

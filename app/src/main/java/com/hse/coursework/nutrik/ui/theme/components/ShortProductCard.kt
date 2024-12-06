package com.hse.coursework.nutrik.ui.theme.components

import android.util.Log
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Error
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import com.hse.coursework.nutrik.model.Product


@Composable
fun ShortProductCard(
    product: Product,
    isForbidden: Boolean,
    navController: NavController
) {
    Log.e("ShortProductCard", "Product: ${product.id}")
    Log.e("ShortProductCard", "Product: ${product}")
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp)
            .clickable { navController.navigate("product_detail_screen/${product.id}") },
        shape = RoundedCornerShape(16.dp),
        elevation =  CardDefaults.cardElevation(4.dp)
    ) {

        Box(
            modifier = Modifier
                .background(Color(0xFFE6E6E6))
                .padding(16.dp)
        ) {
        Row(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = product.name,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "Ккал: ${product.energyValue}, Б: ${product.proteins}г, Ж: ${product.fats}г, У: ${product.carbs}г",
                    fontSize = 14.sp,
                    color = Color.Gray
                )
            }
            if (isForbidden) {
                Icon(
                    imageVector = Icons.Default.Error,
                    contentDescription = "Forbidden Icon",
                    tint = Color(0xFFFF5139),
                    modifier = Modifier.align(Alignment.Bottom).size(28.dp)
                        .padding(start = 8.dp)
                )
            }
        }}
    }
}

@Preview(showBackground = true)
@Composable
fun ProductCardPreview() {
    val mockProduct = Product(
        code = "1",
        name = "Brötchen",
        energyValue = 200.0,
        proteins = 5.0,
        fats = 2.0,
        carbs = 40.0,
    )

    MaterialTheme {
        ShortProductCard(
            product = mockProduct,
            isForbidden = true,
            navController = rememberNavController()

        )
    }
}

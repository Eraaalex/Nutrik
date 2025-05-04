package com.hse.coursework.nutrik.ui.theme.components.camera

import android.graphics.Bitmap
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Card
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.hse.coursework.nutrik.model.ProductEntity
import com.hse.coursework.nutrik.ui.theme.components.BottomNavigationBar


@Composable
fun CapturedPhotoView(
    navController: NavController,
    photoBitmap: Bitmap?,
    foundProduct: ProductEntity?,
    scanning: Boolean,
    message: String?,
    onNavigateToProduct: (String) -> Unit,
    onRetake: () -> Unit
) {
    Scaffold(
        bottomBar = {
            BottomNavigationBar(navController = navController)
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(Color(0xFFfffcdf))
                .padding(
                    start = 16.dp,
                    end = 16.dp,
                    top = 24.dp,
                    bottom = innerPadding.calculateBottomPadding()
                )
        ) {
            photoBitmap?.let { bmp ->
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(350.dp)
                        .clip(RoundedCornerShape(16.dp))
                        .background(Color(0xFFFDF6C8))
                        .padding(4.dp)
                ) {
                    Image(
                        bitmap = bmp.asImageBitmap(),
                        contentDescription = "Сделанное фото",
                        contentScale = ContentScale.Crop,
                        modifier = Modifier
                            .fillMaxSize()
                            .clip(RoundedCornerShape(12.dp))
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            Card(
                modifier = Modifier.fillMaxWidth(),
                elevation = 4.dp,
                backgroundColor = Color(0xFFFDF6C8)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    when {
                        scanning -> {
                            Text(
                                "Идет анализ изображения...",
                                style = MaterialTheme.typography.body1
                            )
                        }

                        foundProduct != null -> {
                            Text(
                                "Продукт: ${foundProduct.name}",
                                style = MaterialTheme.typography.h6,
                                color = Color(0xFF4E2215)
                            )
                            Text(
                                "Штрих-код: ${foundProduct.code}",
                                style = MaterialTheme.typography.h6,
                                color = Color(0xFF4E2215)
                            )
                        }

                        message != null -> {
                            Text(message, style = MaterialTheme.typography.body1, color = Color.Red)
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            if (foundProduct != null && !scanning){
                Button(
                    onClick = {
                        onNavigateToProduct(foundProduct.id)
                    },
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFDF6C8)),
                    shape = RoundedCornerShape(16.dp),
                    contentPadding = PaddingValues(vertical = 24.dp),
                    elevation = ButtonDefaults.buttonElevation(defaultElevation = 0.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center
                    ) {
                        Spacer(modifier = Modifier.width(12.dp))
                        androidx.compose.material3.Text(
                            text = "Перейти к карточке продукта",
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF4E2215),
                            fontSize = 16.sp
                        )
                    }
                }

            }

            Spacer(modifier = Modifier.height(16.dp))

            Button(
                onClick = onRetake,
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFDF6C8)),
                shape = RoundedCornerShape(16.dp),
                contentPadding = PaddingValues(vertical = 24.dp),
                elevation = ButtonDefaults.buttonElevation(defaultElevation = 0.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center
                ) {
                    Spacer(modifier = Modifier.width(12.dp))
                    androidx.compose.material3.Text(
                        text = "Переснять",
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF4E2215),
                        fontSize = 16.sp
                    )
                }
            }
        }
    }
}

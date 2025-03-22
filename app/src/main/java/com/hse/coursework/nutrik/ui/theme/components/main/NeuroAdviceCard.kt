package com.hse.coursework.nutrik.ui.theme.components.main

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.CircularProgressIndicator
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.hse.coursework.nutrik.R

@Composable
fun NeuroAdviceCard(
    adviceText: String,
    modifier: Modifier = Modifier
) {
    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFFFDF6C8)),
        elevation = CardDefaults.cardElevation(8.dp),
        modifier = modifier
            .padding(horizontal = 16.dp, vertical = 8.dp)
            .fillMaxWidth()
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = "Совет от нейросети",
                    style = MaterialTheme.typography.titleMedium,
                    color = Color(0xFF4A3D2E)
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = adviceText,
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color(0xFF4A3D2E)
                )
            }
            Spacer(modifier = Modifier.width(16.dp))
            Box {
                Image(
                    painter = painterResource(R.drawable.neuro_nutria),
                    contentDescription = "Нутрия",
                    modifier = Modifier
                        .size(44.dp)
                )
            }

        }
    }
}

@Preview
@Composable
fun NeuroAdviceCardPreview() {
    NeuroAdviceCard(
        adviceText = "Пейте больше воды и не забывайте про физическую активность!"
    )
}


@Composable
fun EmptyNeuroAdviceCard(
    modifier: Modifier = Modifier
) {
    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFFFDF6C8)),
        modifier = modifier
            .padding(horizontal = 16.dp, vertical = 8.dp)
            .fillMaxWidth()
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(
                modifier = Modifier.weight(1f),
            ) {
                Text(
                    text = "Совет от нейросети",
                    style = MaterialTheme.typography.titleMedium,
                    color = Color(0xFF4A3D2E)
                )
                Spacer(modifier = Modifier.height(8.dp))
                Row() {
                    CircularProgressIndicator(
                        color = Color(0xFFb4d99e),
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Text(
                        "Генерация совета...",
                        style = MaterialTheme.typography.bodyMedium,
                        color = Color(0xFF4A3D2E)
                    )
                }

            }
            Spacer(modifier = Modifier.width(16.dp))
            Box {
                Image(
                    painter = painterResource(R.drawable.neuro_nutria),
                    contentDescription = "Нутрия",
                    modifier = Modifier
                        .size(44.dp)
                )
            }

        }
    }
}

@Preview
@Composable
fun EmptyNeuroAdviceCardPreview() {
    EmptyNeuroAdviceCard(

    )
}
package com.hse.coursework.nutrik.ui.theme.components.main

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Card
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.hse.coursework.nutrik.model.Consumption
import java.time.LocalDate
import java.time.format.DateTimeFormatter

@Composable
fun FoodDiaryTable(
    weeklyConsumption: List<Consumption>,
    selectedDate: LocalDate
) {
    val entriesForSelectedDate = remember(weeklyConsumption, selectedDate) {
        weeklyConsumption.filter { it.date == selectedDate }
    }

    Card(
        elevation = 4.dp,
        modifier = Modifier.background(Color(0xFFFBFBE6)),
        shape = RoundedCornerShape(16.dp),
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(Color(0xFFFBFBE6))
                .padding(16.dp),
        ) {
            Text(
                text = "Дневник питания: ${selectedDate.format(DateTimeFormatter.ofPattern("dd.MM.yyyy"))}",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF4E2215),
                modifier = Modifier.padding(bottom = 12.dp)
            )

            if (entriesForSelectedDate.isEmpty()) {
                Text(
                    text = "Нет записей за этот день",
                    fontSize = 14.sp,
                    color = Color.Gray
                )
            } else {
                entriesForSelectedDate.forEach { entry ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(Color(0xFFEAFBD6), shape = RoundedCornerShape(8.dp))
                            .padding(12.dp)
                            .padding(vertical = 8.dp)
                            .padding(horizontal = 8.dp)
                            .padding(bottom = 8.dp)
                            .padding(top = 8.dp)
                            .padding(end = 8.dp)
                            .padding(start = 8.dp)
                            .padding(0.dp),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = entry.productName,
                            fontSize = 16.sp,
                            color = Color(0xFF3D2C1E)
                        )
                        Text(
                            text = "${entry.weight} г",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = Color(0xFF3D2C1E)
                        )
                    }

                    Spacer(modifier = Modifier.height(8.dp))
                }
            }
        }
    }

}

package com.hse.coursework.nutrik.ui.theme.components.main

import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.Card
import androidx.compose.material.DismissDirection
import androidx.compose.material.DismissValue
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.Icon
import androidx.compose.material.OutlinedTextField
import androidx.compose.material.SwipeToDismiss
import androidx.compose.material.TextFieldDefaults
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.rememberDismissState
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.hse.coursework.nutrik.model.Consumption
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableStateListOf


@OptIn(ExperimentalMaterialApi::class)
@Composable
fun FoodDiaryTable(
    weeklyConsumption: List<Consumption>,
    selectedDate: LocalDate,
    onUpdateConsumption: (Consumption, Double) -> Unit,
    onDeleteConsumption: (Consumption) -> Unit
) {

    val entriesForSelectedDate = remember(weeklyConsumption, selectedDate) {
        weeklyConsumption.filter { it.date == selectedDate }
    }
    val aggregatedEntries = remember(entriesForSelectedDate) {
        entriesForSelectedDate
            .groupBy { it.productName }
            .map { (name, entries) ->
                Consumption(
                    productId   = entries.first().productId,
                    productName = name,
                    date        = selectedDate,
                    weight      = entries.sumOf { it.weight },
                    userId      = entries.first().userId
                )
            }
    }

    val currentEntries = remember { mutableStateListOf<Consumption>() }
    LaunchedEffect(aggregatedEntries) {
        currentEntries.clear()
        currentEntries.addAll(aggregatedEntries)
    }

    var editingEntry by remember { mutableStateOf<Consumption?>(null) }
    var weightInput by remember { mutableStateOf("") }

    Card(
        elevation = 4.dp,
        modifier = Modifier
            .fillMaxWidth()
            .background(Color(0xFFFBFBE6)),
        shape = RoundedCornerShape(16.dp),
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(Color(0xFFFBFBE6))
                .padding(16.dp),
        ) {
            Text(
                text = "Дневник питания: ${
                    selectedDate.format(DateTimeFormatter.ofPattern("dd.MM.yyyy"))
                }",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF4E2215),
                modifier = Modifier.padding(bottom = 12.dp)
            )

            if (currentEntries.isEmpty()) {
                Text(
                    text = "Нет записей за этот день",
                    fontSize = 14.sp,
                    color = Color.Gray
                )
            } else {
                currentEntries.forEach { entry ->
                    key(entry.productId) {
                        val dismissState = rememberDismissState { dismissValue ->
                            if (dismissValue == DismissValue.DismissedToStart) {
                                currentEntries.remove(entry)
                                onDeleteConsumption(entry)
                                true
                            } else {
                                false
                            }
                        }

                        SwipeToDismiss(
                            state = dismissState,
                            directions = setOf(DismissDirection.EndToStart),
                            background = {
                                val color by animateColorAsState(
                                    targetValue = if (dismissState.targetValue == DismissValue.DismissedToStart)
                                        Color(0xFFFFCDD2) else Color.Transparent
                                )
                                Box(
                                    Modifier
                                        .fillMaxSize()
                                        .background(color)
                                        .padding(end = 20.dp),
                                    contentAlignment = Alignment.CenterEnd
                                ) {
                                    Icon(
                                        Icons.Default.Delete,
                                        contentDescription = "Удалить",
                                        tint = Color(0xFFD32F2F)
                                    )
                                }
                            },
                            dismissContent = {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .background(Color(0xFFEAFBD6), RoundedCornerShape(8.dp))
                                        .clickable {
                                            editingEntry = entry
                                            weightInput = entry.weight.toString()
                                        }
                                        .padding(12.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = entry.productName,
                                        fontSize = 16.sp,
                                        color = Color(0xFF3D2C1E),
                                        modifier = Modifier
                                            .weight(1f)
                                            .padding(end = 8.dp),
                                        maxLines = 2,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                    Text(
                                        text = "${entry.weight} г",
                                        fontSize = 16.sp,
                                        fontWeight = FontWeight.SemiBold,
                                        color = Color(0xFF3D2C1E)
                                    )
                                }
                            }
                        )
                        Spacer(Modifier.height(8.dp))
                    }
                }
            }
        }
    }

    editingEntry?.let { entry ->
        AlertDialog(
            onDismissRequest = { editingEntry = null },
            title = { Text("Изменить вес", color = Color(0xFF4E7344)  ) },
            text = {
                Column {
                    Text(entry.productName, fontWeight = FontWeight.Bold, color = Color(0xFF4E7344))
                    Spacer(Modifier.height(8.dp))
                    OutlinedTextField(
                        value = weightInput,
                        onValueChange = { new ->
                            weightInput = new.filter { it.isDigit() }
                        },
                        label = { Text("Вес (г)", color = Color(0xFF4E7344)) },
                        singleLine = true,
                        keyboardOptions = KeyboardOptions.Default.copy(
                            keyboardType = KeyboardType.Number
                        ),
                        colors = TextFieldDefaults.outlinedTextFieldColors(
                            focusedBorderColor = Color(0xFF4E7344),
                            unfocusedBorderColor = Color.Gray,
                            cursorColor = Color(0xFF4E7344)
                        )
                    )
                }
            },
            confirmButton = {
                TextButton(onClick = {
                    weightInput.toDoubleOrNull()?.let { w ->
                        onUpdateConsumption(entry, w)
                    }
                    editingEntry = null
                }) {
                    Text("Сохранить",color = Color(0xFF4E7344))
                }
            },
            dismissButton = {
                TextButton(onClick = {
                    currentEntries.remove(entry)
                    onDeleteConsumption(entry)
                    editingEntry = null
                }) {
                    Text("Удалить",color = Color(0xFF4E7344))
                }
            },
            shape = RoundedCornerShape(16.dp),
            containerColor = Color(0xFFFDFDEB)
        )
    }
}
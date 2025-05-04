package com.hse.coursework.nutrik.ui.theme.components.main


import android.app.DatePickerDialog
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.animation.SizeTransform
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.with
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.CircularProgressIndicator
import androidx.compose.material.Icon
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
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
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.hse.coursework.nutrik.R
import com.hse.coursework.nutrik.model.Consumption
import com.hse.coursework.nutrik.model.ProgressItem
import com.hse.coursework.nutrik.ui.theme.screen.main.ProgressUiState
import com.hse.coursework.nutrik.utils.ColorUtil
import java.time.LocalDate
import java.time.format.DateTimeFormatter


@Composable
fun ScanButton(onClick: () -> Unit) {
    Button(
        onClick = { onClick() },
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = Color(0xFFFDF6C8)
        ),
        shape = RoundedCornerShape(16.dp),
        contentPadding = PaddingValues(vertical = 24.dp),
        elevation = ButtonDefaults.buttonElevation(defaultElevation = 0.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            Icon(
                painter = painterResource(id = R.drawable.barcode),
                contentDescription = "Сканер",
                tint = Color(0xFF4E2215),
                modifier = Modifier.size(24.dp)
            )
            Spacer(modifier = Modifier.width(12.dp))
            Text(
                text = "Сканировать товар",
                fontWeight = FontWeight.Bold,
                color = Color(0xFF4E2215),
                fontSize = 16.sp
            )
        }
    }
}


@OptIn(ExperimentalAnimationApi::class)
@Composable
fun ProgressContent(
    uiState: ProgressUiState,
    onSelectDate: (LocalDate) -> Unit,
    weeklyConsumption: List<Consumption>,
    onUpdateConsumption: (Consumption, Double) -> Unit,
    onDeleteConsumption: (Consumption) -> Unit
) {

    AnimatedContent(
        targetState = uiState.selectedDate,
        transitionSpec = {

            if (targetState > initialState) {
                slideInHorizontally { width -> width } + fadeIn() with
                        slideOutHorizontally { width -> -width } + fadeOut()
            } else {
                slideInHorizontally { width -> -width } + fadeIn() with
                        slideOutHorizontally { width -> width } + fadeOut()
            }
                .using(SizeTransform(false))
        }, label = ""
    ) { animatedDate ->
        ContentByDate(
            date = animatedDate,
            uiState = uiState,
            weeklyConsumption = weeklyConsumption,
            onSelectDate = onSelectDate,
            onUpdateConsumption = onUpdateConsumption,
            onDeleteConsumption = onDeleteConsumption,
        )


    }
}

@Composable
fun ContentByDate(
    date: LocalDate ,
    uiState: ProgressUiState,
    weeklyConsumption: List<Consumption>,
    onSelectDate: (LocalDate) -> Unit,
    onUpdateConsumption: (Consumption, Double) -> Unit,
    onDeleteConsumption: (Consumption) -> Unit,
) {

    val dateFormatter = remember { DateTimeFormatter.ofPattern("dd.MM.yyyy") }
    val selectedDate = uiState.selectedDate
    val progress = uiState.progress

    var showDatePicker by remember { mutableStateOf(false) }
    val context = LocalContext.current

    if (showDatePicker) {
        val initialYear = selectedDate.year
        val initialMonth = selectedDate.monthValue - 1
        val initialDay = selectedDate.dayOfMonth

        DatePickerDialog(
            context,
            { _, year, month, dayOfMonth ->
                val newDate = LocalDate.of(year, month + 1, dayOfMonth)
                onSelectDate(newDate)
            },
            initialYear,
            initialMonth,
            initialDay
        ).apply {
            setOnDismissListener { showDatePicker = false }
        }.show()
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .pointerInput(selectedDate) {
                detectHorizontalDragGestures { change, dragAmount ->
                    change.consume()
                    if (dragAmount < -50) {
                        val nextDate = selectedDate.plusDays(1)
                        if (!nextDate.isAfter(LocalDate.now())) {
                            onSelectDate(nextDate)
                        }
                    } else if (dragAmount > 50) {
                        onSelectDate(selectedDate.minusDays(1))
                    }
                }
            }
            .padding(16.dp)
    ) {

        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween,
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 8.dp)
        ) {
            Text(
                text = selectedDate.format(dateFormatter),
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold
            )
            Icon(
                imageVector = Icons.Filled.CalendarToday,
                contentDescription = "Выбрать дату",
                modifier = Modifier
                    .size(32.dp)
                    .clickable { showDatePicker = true }
            )
        }

        progress?.let { data ->
            val items = listOf(
                ProgressBarCardData(
                    "Белки",
                    "${data.protein.toInt()} г",
                    data.protein / 100f,
                    Color(0xFF4CAF50)
                ),
                ProgressBarCardData(
                    "Жиры",
                    "${data.fat.toInt()} г",
                    data.fat / 100f,
                    Color(0xFF4CAF50)
                ),
                ProgressBarCardData(
                    "Углеводы",
                    "${data.carbs.toInt()} г",
                    data.carbs / 300f,
                    Color(0xFFFF9800)
                ),
                ProgressBarCardData(
                    "Калории",
                    "${data.calories.toInt()}",
                    data.calories / 2000f,
                    Color(0xFF2196F3)
                ),
                ProgressBarCardData(
                    "Сахар",
                    "${if (data.sugar > 0) data.sugar.toInt() else 0} г",
                    data.sugar / 30f,
                    Color(0xFFF44336)
                ),
                ProgressBarCardData(
                    "Соль",
                    "${if (data.salt > 0) data.salt.toInt() else 0} г",
                    data.salt / 5f,
                    Color(0xFF4CAF50)
                )
            )

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items.chunked(2).forEach { rowItems ->
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        rowItems.forEach { item ->
                            Box(modifier = Modifier.weight(1f)) {
                                ProgressBarCard(
                                    title = item.title,
                                    value = item.value,
                                    progress = item.progress,
                                    progressColor = item.progressColor
                                )
                            }
                        }
                        if (rowItems.size < 2) {
                            Spacer(modifier = Modifier.weight(1f))
                        }
                    }
                }


                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Box(modifier = Modifier.weight(1f)) {
                        ViolationCard(
                            violationCount = data.violationsCount,
                            modifier = Modifier
                                .fillMaxWidth()
                                .aspectRatio(1f)
                        )
                    }
                    Spacer(modifier = Modifier.weight(1f))
                }


                if (weeklyConsumption.isNotEmpty()) {
                    FoodDiaryTable(
                        weeklyConsumption = weeklyConsumption,
                        selectedDate = uiState.selectedDate,
                        onUpdateConsumption = { entry, newWeight ->
                            onUpdateConsumption(entry, newWeight)
                        },
                        onDeleteConsumption = { entry ->
                            onDeleteConsumption(entry)
                        }
                    )
                }
            }


        } ?: run {
            Text(
                text = "Загрузка данных...",
                style = MaterialTheme.typography.bodyLarge
            )
        }
    }
}


@Composable
fun ProgressBarCard(
    title: String,
    value: String,
    progress: Double,
    progressColor: Color
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .aspectRatio(1f),
        colors = CardDefaults.cardColors(containerColor = Color(0xFFFBFBE6)),
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Box(modifier = Modifier.size(96.dp), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(
                    progress = 1f,
                    strokeWidth = 8.dp,
                    color = ColorUtil.lightenColor(progressColor, 0.4f),
                    modifier = Modifier.fillMaxSize()
                )
                CircularProgressIndicator(
                    progress = progress.coerceIn(0.0, 1.0).toFloat(),
                    strokeWidth = 8.dp,
                    color = progressColor,
                    modifier = Modifier.fillMaxSize()
                )
                Text(
                    text = value,
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold,
                    color = progressColor
                )
            }
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = title,
                fontSize = 16.sp,
                color = Color(0xFF3D2C1E),
                fontWeight = FontWeight.Medium,
                textAlign = TextAlign.Center
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
fun ProgressBarCardPreview() {
    ProgressBarCard(
        title = "Белки",
        value = "80 г",
        progress = 0.8,
        progressColor = Color(0xFF4CAF50)
    )
}

data class ProgressBarCardData(
    val title: String,
    val value: String,
    val progress: Double,
    val progressColor: Color
)

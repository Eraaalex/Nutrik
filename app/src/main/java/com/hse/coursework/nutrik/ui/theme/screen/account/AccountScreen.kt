package com.hse.coursework.nutrik.ui.theme.screen.account

import android.widget.NumberPicker
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.Icon
import androidx.compose.material.TopAppBar
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.hse.coursework.nutrik.R
import com.hse.coursework.nutrik.model.Restriction
import com.hse.coursework.nutrik.model.dto.Gender
import com.hse.coursework.nutrik.navigation.Screen
import com.hse.coursework.nutrik.ui.theme.components.account.RestrictionSelector

@Composable
fun AccountScreen(
    navController: NavController,
    viewModel: AccountViewModel = hiltViewModel()
) {
    var showAgeDialog by remember { mutableStateOf(false) }
    var showGenderDialog by remember { mutableStateOf(false) }
    val user by viewModel.user
    val scrollState = rememberScrollState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        "Аккаунт",
                        color = Color(0xFF4E7344),
                        fontWeight = FontWeight.Bold
                    )
                },
                backgroundColor = Color(0xFFFDFDEB),
                navigationIcon = {
                    IconButton(onClick = { navController.navigate(Screen.Main.route) }) {
                        Icon(
                            Icons.Default.ArrowBack,
                            contentDescription = "Назад",
                            tint = Color(0xFF4E7344)
                        )
                    }
                }
            )
        },
        containerColor = Color(0xFFEAFBD6)
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .padding(innerPadding)
                .padding(16.dp)
                .fillMaxSize()
                .verticalScroll(scrollState),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color(0xFFFDFDEB), shape = RoundedCornerShape(24.dp))
                    .padding(16.dp)
            ) {
                Image(
                    painter = painterResource(id = R.drawable.neuro_nutria),
                    contentDescription = "Avatar",
                    modifier = Modifier
                        .size(72.dp)
                        .clip(CircleShape)
                )
                Spacer(modifier = Modifier.width(16.dp))
                Text(
                    text = user.email.substringBefore("@"),
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF4E7344)
                )
            }

            InfoItem("Возраст", user.age.toString()) { showAgeDialog = true }
            InfoItem("Пол", genderToText(user.gender)) { showGenderDialog = true }
            RestrictionSelector(user.restrictions, onToggleRestriction = { restriction ->
                viewModel.toggleRestriction(restriction)
            })
            InfoItem("Выйти из аккаунта", "") {
                viewModel.logout()
                navController.navigate(Screen.Auth.route)
            }

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable {

                    }
                    .padding(vertical = 16.dp),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    "Другие элементы",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF4E7344)
                )
                Icon(
                    Icons.Default.ArrowForward,
                    contentDescription = null,
                    tint = Color(0xFF4E7344)
                )
            }
        }

        if (showAgeDialog) {
            var selectedAge by remember { mutableStateOf(user.age) }
            AlertDialog(

                onDismissRequest = { showAgeDialog = false },
                title = {
                    Text(
                        text = "Выберите возраст",
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF4E7344)
                    )
                },
                text = {

                    AndroidView(
                        factory = { context ->
                            NumberPicker(context).apply {
                                minValue = 5
                                maxValue = 120
                                value = user.age
                                wrapSelectorWheel = false

                                setOnValueChangedListener { _, _, newVal ->
                                    selectedAge = newVal
                                }

                                descendantFocusability = NumberPicker.FOCUS_BLOCK_DESCENDANTS
                            }
                        },
                        update = {
                            it.value = selectedAge
                        },
                        modifier = Modifier
                            .padding(top = 8.dp)
                            .fillMaxWidth()
                            .height(180.dp)
                    )
                },
                confirmButton = {
                    TextButton(onClick = {
                        viewModel.updateAge(selectedAge)
                        showAgeDialog = false
                    }) {
                        Text("Сохранить", fontWeight = FontWeight.Bold, color = Color(0xFF4E7344))
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showAgeDialog = false }) {
                        Text("Отмена", fontWeight = FontWeight.Bold, color = Color(0xFF4E7344))
                    }
                },
                shape = RoundedCornerShape(16.dp),
                containerColor = Color(0xFFFDFDEB)
            )
        }


        if (showGenderDialog) {
            AlertDialog(
                containerColor = Color(0xFFFDFDEB),
                onDismissRequest = { showGenderDialog = false },
                title = {
                    Text(
                        "Выберите пол",
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF4E7344)
                    )
                },
                text = {
                    Column {
                        Gender.values().forEach { gender ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable {
                                        viewModel.updateGender(gender)
                                        showGenderDialog = false
                                    }
                                    .padding(8.dp)
                            ) {
                                Text(
                                    genderToText(gender),
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFF4E7344)
                                )
                            }
                        }
                    }
                },
                confirmButton = {},
                dismissButton = {
                    TextButton(onClick = { showGenderDialog = false }) {
                        Text(
                            "Отмена",
                            color = Color(0xFF4E7344)
                        )
                    }
                }
            )
        }

    }
}

@Composable
fun InfoItem(
    label: String,
    value: String? = null,
    onClick: (() -> Unit)? = null
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color(0xFFFDFDEB), shape = RoundedCornerShape(20.dp))
            .clickable(enabled = onClick != null) { onClick?.invoke() }
            .padding(horizontal = 16.dp, vertical = 20.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = label,
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold,
            color = Color(0xFF4E7344)
        )
        if (value != null) {
            Text(
                text = value,
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF4E7344)
            )
        }
    }
}

fun genderToText(gender: Gender): String = when (gender) {
    Gender.MALE -> "Мужской"
    Gender.FEMALE -> "Женский"
    Gender.OTHER -> "Другое"
    Gender.UNSPECIFIED -> "Не указано"
}


@Composable
fun RestrictionItem(
    selected: List<Restriction>,
    onToggleRestriction: (Restriction) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color(0xFFFDFDEB), shape = RoundedCornerShape(20.dp))
            .padding(16.dp),

        ) {
        Text(
            text = "Пищевые ограничения",
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold,
            color = Color(0xFF4E7344)
        )
        Spacer(Modifier.height(8.dp))

        Restriction.values().filter { it != Restriction.NONE }.forEach { restriction ->
            val checked = restriction in selected

            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onToggleRestriction(restriction) }
                    .padding(vertical = 4.dp)
            ) {
                Checkbox(
                    checked = checked,
                    onCheckedChange = { onToggleRestriction(restriction) },
                    colors = CheckboxDefaults.colors(checkedColor = Color(0xFF4E7344))
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = restriction.russianName,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF4E7344)
                )
            }
        }
    }
}
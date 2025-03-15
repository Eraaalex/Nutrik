package com.hse.coursework.nutrik.ui.theme.components.account

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.hse.coursework.nutrik.model.dto.Gender

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


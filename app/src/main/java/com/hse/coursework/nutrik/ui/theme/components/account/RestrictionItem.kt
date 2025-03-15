package com.hse.coursework.nutrik.ui.theme.components.account

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.hse.coursework.nutrik.model.Restriction


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
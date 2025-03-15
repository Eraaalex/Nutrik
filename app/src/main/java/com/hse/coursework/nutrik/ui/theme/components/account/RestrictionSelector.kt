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
import androidx.compose.material.MaterialTheme
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.Divider
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.hse.coursework.nutrik.model.Restriction


@Composable
fun RestrictionSelector(
    selected: List<Restriction>,
    onToggleRestriction: (Restriction) -> Unit
) {
    val groups = listOf(
        "Аллергии" to listOf(
            Restriction.NUT,
            Restriction.SEAFOOD,
            Restriction.EGG,
            Restriction.LACTOSE
        ),
        "Нутриенты" to listOf(Restriction.SALT, Restriction.SUGAR, Restriction.HIGH_CARBOHYDRATE),
        "Ограничения на мясные продукты" to listOf(Restriction.VEGETARIAN, Restriction.VEGAN),
    )

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color(0xFFF9FFE9), shape = RoundedCornerShape(20.dp))
            .padding(16.dp)
    ) {
        Text(
            text = "Пищевые ограничения",
            style = MaterialTheme.typography.h6.copy(
                color = Color(0xFF4E7344),
                fontWeight = FontWeight.Bold
            )
        )

        Spacer(modifier = Modifier.height(12.dp))

        groups.forEach { (groupName, restrictions) ->
            Text(
                text = groupName,
                style = MaterialTheme.typography.subtitle1.copy(
                    color = Color(0xFF4E7344),
                    fontWeight = FontWeight.SemiBold
                ),
                modifier = Modifier.padding(vertical = 8.dp)
            )

            restrictions.forEach { restriction ->
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
                        fontWeight = FontWeight.Medium,
                        color = Color(0xFF4E7344)
                    )
                }
            }

            Divider(
                color = Color(0xFFB7D6AF),
                thickness = 1.dp,
                modifier = Modifier.padding(vertical = 8.dp)
            )
        }
    }
}

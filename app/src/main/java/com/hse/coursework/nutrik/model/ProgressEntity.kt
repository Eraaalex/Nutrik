package com.hse.coursework.nutrik.model

import androidx.room.Entity
import com.hse.coursework.nutrik.model.ProgressItem
import com.hse.coursework.nutrik.model.Restriction
import java.time.LocalDate

@Entity(
    tableName = "progress_items",
    primaryKeys = ["userId", "date"]
)
data class ProgressEntity(
    val userId: String,
    val date: String,
    val protein: Double,
    val fat: Double,
    val carbs: Double,
    val calories: Double,
    val sugar: Double,
    val salt: Double,
    val violationsCount: Int,
    val violations: List<Restriction> = emptyList()
) {
    fun toDomain(): ProgressItem =
        ProgressItem(
            date = LocalDate.parse(date),
            protein = protein,
            fat = fat,
            carbs = carbs,
            calories = calories,
            sugar = sugar,
            salt = salt,
            violationsCount = violationsCount,
            violations = violations
        )
}

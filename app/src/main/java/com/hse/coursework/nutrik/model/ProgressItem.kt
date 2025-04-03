package com.hse.coursework.nutrik.model

import java.time.LocalDate
import java.time.format.DateTimeFormatter

data class ProgressItem(
    val date: LocalDate,      // Дата, за которую приведены данные
    val protein: Int,         // Белки (граммы)
    val fat: Int,             // Жиры (граммы)
    val carbs: Int,           // Углеводы (граммы)
    val calories: Int,        // Калории (ккал)
    val sugar: Int,           // Сахар (граммы)
    val salt: Int,            // Соль (граммы)
    val violationsCount: Int,  // Количество нарушений
    val violations: List<Restriction> = emptyList() // Список нарушений
) {
    fun toEntity(userId: String, dateKey: String): ProgressEntity {
        return ProgressEntity(
            userId = userId,
            date = dateKey,
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

    fun toRemoteEntity(): ProgressRemoteEntity {
        val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd")
        return ProgressRemoteEntity(
            date = date.format(formatter),
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

    companion object {
        fun fromRemoteEntity(remote: ProgressRemoteEntity): ProgressItem {
            val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd")
            return ProgressItem(
                date = LocalDate.parse(remote.date, formatter),
                protein = remote.protein,
                fat = remote.fat,
                carbs = remote.carbs,
                calories = remote.calories,
                sugar = remote.sugar,
                salt = remote.salt,
                violationsCount = remote.violationsCount
            )
        }
    }
}

data class ProgressRemoteEntity(
    var date: String = "",
    var protein: Int = 0,
    var fat: Int = 0,
    var carbs: Int = 0,
    var calories: Int = 0,
    var sugar: Int = 0,
    var salt: Int = 0,
    var violationsCount: Int = 0,
    var violations: List<Restriction> = emptyList()
)

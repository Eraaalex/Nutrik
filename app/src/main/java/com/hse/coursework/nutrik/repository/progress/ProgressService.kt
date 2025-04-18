package com.hse.coursework.nutrik.repository.progress

import com.hse.coursework.nutrik.model.Product
import com.hse.coursework.nutrik.model.ProgressItem
import com.hse.coursework.nutrik.repository.user.UserRepository
import kotlinx.coroutines.flow.firstOrNull
import java.time.LocalDate
import javax.inject.Inject

class ProgressService @Inject constructor(
    private val progressRepository: ProgressRepository,
) {
    suspend fun updateProgress(
        product: Product,
        newWeight: Double,
        userId: String,
        date: LocalDate = LocalDate.now()
    ) {
        val existingProgress = progressRepository.getProgressForDate(userId, date).firstOrNull()

        val weightRatio = newWeight / 100.0

        val addedProtein = (product.proteins * weightRatio).toInt()
        val addedFat = (product.fats * weightRatio).toInt()
        val addedCarbs = (product.carbs * weightRatio).toInt()
        val addedCalories = (product.energyValue * weightRatio).toInt()
        val addedSugar = (product.sugar * weightRatio).toInt()
        val addedSalt = (product.salt * weightRatio).toInt()

        val newProgress = existingProgress?.copy(
            protein = existingProgress.protein + addedProtein,
            fat = existingProgress.fat + addedFat,
            carbs = existingProgress.carbs + addedCarbs,
            calories = existingProgress.calories + addedCalories,
            sugar = existingProgress.sugar + addedSugar,
            salt = existingProgress.salt + addedSalt
        )
            ?: ProgressItem(
                date = date,
                protein = addedProtein,
                fat = addedFat,
                carbs = addedCarbs,
                calories = addedCalories,
                sugar = addedSugar,
                salt = addedSalt,
                violationsCount = 0
            )

        progressRepository.saveProgress(userId, newProgress)
        progressRepository.fetchInitialDataForLastWeek(userId)
    }

    private fun getViolationCount(
        product: Product,
        progress: ProgressItem
    ): Int {
        var violationsCount = 0



        return violationsCount
    }


}
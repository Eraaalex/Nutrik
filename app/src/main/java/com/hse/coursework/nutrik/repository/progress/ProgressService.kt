package com.hse.coursework.nutrik.repository.progress

import android.util.Log
import com.hse.coursework.nutrik.model.Product
import com.hse.coursework.nutrik.model.ProgressItem
import com.hse.coursework.nutrik.model.Restriction
import kotlinx.coroutines.flow.firstOrNull
import java.time.LocalDate
import javax.inject.Inject
import kotlin.math.roundToInt

class ProgressService @Inject constructor(
    private val progressRepository: ProgressRepository,
) {
    suspend fun updateProgress(
        product: Product,
        newWeight: Double,
        userId: String,
        date: LocalDate = LocalDate.now(),
        user: List<Restriction>
    ) {
        val existing = progressRepository.getProgressForDate(userId, date).firstOrNull()
        val ratio = newWeight / 100.0

        val deltaProtein = (product.proteins * ratio)
        val deltaFat = (product.fats * ratio)
        val deltaCarbs = (product.carbs * ratio)
        val deltaCalories = (product.energyValue * ratio)
        val deltaSugar = (product.sugar * ratio)
        val deltaSalt = (product.salt * ratio)

        val updated = existing?.copy(
            protein = (existing.protein + deltaProtein).coerceAtLeast(0.0),
            fat = (existing.fat + deltaFat).coerceAtLeast(0.0),
            carbs = (existing.carbs + deltaCarbs).coerceAtLeast(0.0),
            calories = (existing.calories + deltaCalories).coerceAtLeast(0.0),
            sugar = (existing.sugar + deltaSugar).coerceAtLeast(0.0),
            salt = (existing.salt + deltaSalt).coerceAtLeast(0.0),
            violationsCount = getViolationCount(product, existing, user, newWeight),
            date = date
        ) ?: ProgressItem(
            date = date,
            protein = deltaProtein,
            fat = deltaFat,
            carbs = deltaCarbs,
            calories = deltaCalories,
            sugar = deltaSugar,
            salt = deltaSalt,
            violationsCount = getViolationCount(product, null, user, newWeight)
        )

        progressRepository.saveProgress(userId, updated)
        progressRepository.fetchInitialDataForLastWeek(userId)
    }

    private fun getViolationCount(
        product: Product,
        progress: ProgressItem?,
        userRestrictions: List<Restriction>,
        newWeight: Double
    ): Int {
        Log.e("ProgressService", "getViolationCount called with product: $product, progress: $progress, userRestrictions: $userRestrictions, newWeight: $newWeight")
        var violationsCount = progress?.violationsCount ?: 0
        Log.e("ProgressService", "Initial violationsCount: $violationsCount")
        if (userRestrictions.isEmpty() || product.allergens.isEmpty()) {
            return violationsCount
        }
Log.e("ProgressService", "User restrictions: $userRestrictions, Product allergens: ${product.allergens}")
        if (newWeight <= 0 && product.allergens.any { it in userRestrictions }) {
            return (--violationsCount).coerceAtLeast(0)
        }
        Log.e("ProgressService", "Checking allergens against user restrictions")
        if (product.allergens.any { it in userRestrictions }) {
            violationsCount++
        }
Log.e("ProgressService", "Final violationsCount: $violationsCount")
        return violationsCount
    }


}
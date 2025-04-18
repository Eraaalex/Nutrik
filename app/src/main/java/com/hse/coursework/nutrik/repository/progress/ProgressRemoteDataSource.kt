package com.hse.coursework.nutrik.repository.progress

import com.hse.coursework.nutrik.model.ProgressItem
import com.hse.coursework.nutrik.service.FirebaseService
import java.time.LocalDate
import javax.inject.Inject


class ProgressRemoteDataSource @Inject constructor(
    private val firebaseService: FirebaseService
) {
    suspend fun getProgressForDate(userId: String, date: LocalDate): ProgressItem? =
        firebaseService.getProgressForDate(userId, date)

    suspend fun saveProgressForDate(userId: String, progressItem: ProgressItem) {
        firebaseService.saveProgressForDate(userId, progressItem)
    }

    suspend fun getProgressForWeek(userId: String): List<WeekProgressResult> {
        val today = LocalDate.now()
        val weekAgo = today.minusDays(6)
        return firebaseService.getProgressForPeriod(userId, weekAgo, today)
            .map { WeekProgressResult(it.first, it.second) }
    }
}

data class WeekProgressResult(val date: LocalDate, val progress: ProgressItem)

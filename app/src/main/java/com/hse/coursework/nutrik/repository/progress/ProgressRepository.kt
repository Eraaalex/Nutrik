package com.hse.coursework.nutrik.repository.progress

import android.util.Log
import com.hse.coursework.nutrik.data.dao.ProgressDao
import com.hse.coursework.nutrik.model.ProgressItem
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.withContext
import java.time.LocalDate
import javax.inject.Inject


interface ProgressRepository {
    suspend fun fetchInitialDataForLast3Days(userId: String)
    fun getProgressForDate(userId: String, date: LocalDate): Flow<ProgressItem?>
    suspend fun saveProgress(userId: String, progressItem: ProgressItem)
    suspend fun fetchInitialDataForLastWeek(userId: String): List<WeekProgressResult>
}

class ProgressRepositoryImpl @Inject constructor(
    private val dao: ProgressDao,
    private val remote: ProgressRemoteDataSource
) : ProgressRepository {

    private var weekProgress: List<WeekProgressResult> = emptyList()

    override suspend fun fetchInitialDataForLast3Days(userId: String) {
        val today = LocalDate.now()
        (0..2).forEach { offset ->
            val date = today.minusDays(offset.toLong())
            remote.getProgressForDate(userId, date)
                ?.let { dao.insert(it.toEntity(userId, date.toString())) }
        }
    }

    override fun getProgressForDate(
        userId: String,
        date: LocalDate
    ): Flow<ProgressItem?> = flow {
        val localProgress = dao.getByUserAndDate(userId, date.toString()).firstOrNull()
        if (localProgress != null) {
            emit(localProgress.toDomain())
        } else {
            val remoteProgress = remote.getProgressForDate(userId, date)
            if (remoteProgress != null) {
                dao.insert(remoteProgress.toEntity(userId, date.toString()))
                emit(remoteProgress)
            } else {
                emit(
                    ProgressItem(
                        date = date,
                        protein = 0,
                        fat = 0,
                        carbs = 0,
                        calories = 0,
                        sugar = 0,
                        salt = 0,
                        violationsCount = 0
                    )
                )
            }
        }
    }

    override suspend fun saveProgress(
        userId: String,
        progressItem: ProgressItem
    ) {
        val date = LocalDate.now()
        dao.insert(progressItem.toEntity(userId, date.toString()))
        remote.saveProgressForDate(userId, progressItem)
    }

    override suspend fun fetchInitialDataForLastWeek(userId: String): List<WeekProgressResult> {
        weekProgress = remote.getProgressForWeek(userId)

        withContext(Dispatchers.IO) {
            weekProgress.forEach { (date, progress) ->
                dao.insert(progress.toEntity(userId, date.toString()))
            }
        }
        Log.e("ProgressRepository", "Week progress: $weekProgress")
        return weekProgress
    }

}
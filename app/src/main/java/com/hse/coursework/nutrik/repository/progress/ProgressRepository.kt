package com.hse.coursework.nutrik.repository.progress

import android.util.Log
import com.hse.coursework.nutrik.data.dao.ProgressDao
import com.hse.coursework.nutrik.model.ProgressItem
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onStart
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
    ): Flow<ProgressItem> {
        val dateStr = date.toString()
        return dao
            .getByUserAndDate(userId, dateStr)                // Flow<ProgressEntity?>
            .map { entity ->
                // как только в БД вставят новую запись — сюда приедет новый entity
                entity?.toDomain() ?: ProgressItem(
                    date     = date,
                    protein  = 0.0,
                    fat      = 0.0,
                    carbs    = 0.0,
                    calories = 0.0,
                    sugar    = 0.0,
                    salt     = 0.0,
                    violationsCount = 0
                )
            }
            .onStart {
                // один раз при подписке — попробуем подгрузить из remote, если локально пусто
                val hasLocal = dao.getByUserAndDate(userId, dateStr)
                    .firstOrNull() != null
                if (!hasLocal) {
                    remote.getProgressForDate(userId, date)?.also { remoteItem ->
                        dao.insert(remoteItem.toEntity(userId, dateStr))
                    }
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
package com.hse.coursework.nutrik.repository.source

import com.hse.coursework.nutrik.model.ProgressItem
import com.hse.coursework.nutrik.repository.progress.ProgressRemoteDataSource
import com.hse.coursework.nutrik.repository.progress.WeekProgressResult
import com.hse.coursework.nutrik.service.FirebaseService
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test
import java.time.LocalDate

class ProgressRemoteDataSourceTest {

    private val firebaseService: FirebaseService = mockk(relaxed = true)
    private val dataSource = ProgressRemoteDataSource(firebaseService)

    private val userId = "user123"
    private val date = LocalDate.of(2024, 12, 1)
    private val item = ProgressItem(
        date, protein = 10, fat = 10, carbs = 10,
        calories = 2000, sugar = 10, salt = 10, violationsCount = 0
    )

    @Test
    fun `getProgressForDate returns data from FirebaseService`() = runTest {
        coEvery { firebaseService.getProgressForDate(userId, date) } returns item

        val result = dataSource.getProgressForDate(userId, date)

        assertEquals(item, result)
        coVerify { firebaseService.getProgressForDate(userId, date) }
    }

    @Test
    fun `getProgressForDate returns null if no data`() = runTest {
        coEvery { firebaseService.getProgressForDate(userId, date) } returns null

        val result = dataSource.getProgressForDate(userId, date)

        assertNull(result)
    }

    @Test
    fun `saveProgressForDate delegates to FirebaseService`() = runTest {
        coEvery { firebaseService.saveProgressForDate(userId, item) } returns Unit

        dataSource.saveProgressForDate(userId, item)

        coVerify { firebaseService.saveProgressForDate(userId, item) }
    }

    @Test
    fun `getProgressForWeek returns mapped results`() = runTest {
        val today = LocalDate.now()
        val weekAgo = today.minusDays(6)
        val firebaseResult = listOf(
            weekAgo to ProgressItem(date, protein = 10, fat = 10, carbs = 10,
                calories = 2000, sugar = 10, salt = 10, violationsCount = 0),
            today to ProgressItem(date, protein = 100, fat = 20, carbs = 20,
                calories = 1000, sugar = 20, salt = 20, violationsCount = 2)
        )

        coEvery {
            firebaseService.getProgressForPeriod(
                userId,
                weekAgo,
                today
            )
        } returns firebaseResult

        val result = dataSource.getProgressForWeek(userId)

        assertEquals(2, result.size)
        assertEquals(WeekProgressResult(weekAgo, firebaseResult[0].second), result[0])
        assertEquals(WeekProgressResult(today, firebaseResult[1].second), result[1])
    }
}

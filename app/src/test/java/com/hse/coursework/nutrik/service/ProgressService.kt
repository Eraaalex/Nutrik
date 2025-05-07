package com.hse.coursework.nutrik.service


import com.hse.coursework.nutrik.model.Product
import com.hse.coursework.nutrik.model.ProgressItem
import com.hse.coursework.nutrik.repository.progress.ProgressRepository
import com.hse.coursework.nutrik.repository.progress.ProgressService
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Test
import java.time.LocalDate

class ProgressServiceTest {

    private val progressRepository: ProgressRepository = mockk(relaxed = true)
    private val service = ProgressService(progressRepository)

    private val userId = "user123"
    private val date = LocalDate.of(2024, 5, 25)

    private val product = Product(
        id = "1",
        code = "111",
        name = "TestProduct",
        unit = "g",
        proteins = 10.0,
        fats = 5.0,
        carbs = 15.0,
        energyValue = 200.0,
        sugar = 3.0,
        salt = 1.0
    )

    @Test
    fun `updateProgress creates new progress if none exists`() = runTest {
        coEvery { progressRepository.getProgressForDate(userId, date) } returns flowOf(null)

        service.updateProgress(product, 150.0, userId, date)

        val expected = ProgressItem(
            date = date,
            protein = 15,
            fat = 7,
            carbs = 22,
            calories = 300,
            sugar = 4,
            salt = 1,
            violationsCount = 0
        )

        coVerify {
            progressRepository.saveProgress(userId, expected)
            progressRepository.fetchInitialDataForLastWeek(userId)
        }
    }

    @Test
    fun `updateProgress updates existing progress`() = runTest {
        val existing = ProgressItem(
            date = date,
            protein = 20,
            fat = 10,
            carbs = 30,
            calories = 400,
            sugar = 5,
            salt = 2,
            violationsCount = 0
        )

        coEvery { progressRepository.getProgressForDate(userId, date) } returns flowOf(existing)

        service.updateProgress(product, 100.0, userId, date)

        val expected = existing.copy(
            protein = 30,
            fat = 15,
            carbs = 45,
            calories = 600,
            sugar = 8,
            salt = 3
        )

        coVerify {
            progressRepository.saveProgress(userId, expected)
            progressRepository.fetchInitialDataForLastWeek(userId)
        }
    }
}

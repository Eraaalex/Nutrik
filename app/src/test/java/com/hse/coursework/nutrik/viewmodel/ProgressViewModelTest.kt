@file:OptIn(ExperimentalCoroutinesApi::class)

package com.hse.coursework.nutrik.viewmodel

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.hse.coursework.nutrik.model.Consumption
import com.hse.coursework.nutrik.model.ProgressItem
import com.hse.coursework.nutrik.model.dto.User
import com.hse.coursework.nutrik.repository.chat.ChatRepository
import com.hse.coursework.nutrik.repository.consumption.ConsumptionRepository
import com.hse.coursework.nutrik.repository.progress.ProgressRepository
import com.hse.coursework.nutrik.repository.progress.WeekProgressResult
import com.hse.coursework.nutrik.repository.user.UserRepository
import com.hse.coursework.nutrik.ui.theme.screen.main.ProgressViewModel
import io.mockk.coEvery
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import java.time.LocalDate

class ProgressViewModelTest {

    private lateinit var viewModel: ProgressViewModel
    private val progressRepo: ProgressRepository = mockk()
    private val auth: FirebaseAuth = mockk()
    private val userRepo: UserRepository = mockk()
    private val chatRepo: ChatRepository = mockk()
    private val consumptionRepo: ConsumptionRepository = mockk()
    private val user: FirebaseUser = mockk()

    private val testDispatcher = StandardTestDispatcher()

    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)

        every { auth.currentUser } returns user
        every { user.uid } returns "test_uid"

        coEvery { progressRepo.getProgressForDate(any(), any()) } returns flowOf(null)

        viewModel = ProgressViewModel(
            progressRepo, auth, userRepo, chatRepo, consumptionRepo
        )
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `onSelectDate updates selected date`() = runTest {
        val newDate = LocalDate.now()
        viewModel.onSelectDate(newDate)
        assertEquals(newDate, viewModel.uiState.value.selectedDate)
    }

    @Test
    fun `fetchInitialWeekDataAndTip updates state flows correctly`() = runTest {
        val fakeConsumption = listOf(Consumption())
        val testDate = LocalDate.of(2023, 1, 1)
        val fakeWeekData = listOf(
            WeekProgressResult(
                testDate,
                ProgressItem(
                    testDate,
                    1,
                    2,
                    4,
                    400,
                    10,
                    1,
                    0
                )
            )
        )
        val fakeUser = User()
        val fakeAdvice = "Питайся сбалансированно и не забывай пить воду"

        coEvery {
            consumptionRepo.getConsumptionForWeek(
                any(),
                any(),
                any(),
                any()
            )
        } returns fakeConsumption
        coEvery { progressRepo.fetchInitialDataForLastWeek(any()) } returns fakeWeekData
        coEvery { userRepo.getUser() } returns fakeUser
        coEvery { chatRepo.sendMessage(any()) } returns fakeAdvice

        viewModel.fetchInitialWeekDataAndTip()
        testDispatcher.scheduler.advanceUntilIdle()

        assertEquals(fakeConsumption, viewModel.weeklyConsumption.value)
        assertEquals(fakeWeekData, viewModel.weekProgress.value)
        assertEquals(fakeAdvice, viewModel.neuroAdvice.value)
    }
}

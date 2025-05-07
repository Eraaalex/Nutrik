@file:OptIn(ExperimentalCoroutinesApi::class)

package com.hse.coursework.nutrik.repository

import android.util.Log
import com.hse.coursework.nutrik.network.Choice
import com.hse.coursework.nutrik.network.Message
import com.hse.coursework.nutrik.network.OpenRouterApiService
import com.hse.coursework.nutrik.network.OpenRouterResponse
import com.hse.coursework.nutrik.network.RetrofitInstance
import com.hse.coursework.nutrik.repository.chat.ChatRepository
import io.mockk.coEvery
import io.mockk.every
import io.mockk.mockk
import io.mockk.mockkObject
import io.mockk.mockkStatic
import io.mockk.unmockkAll
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test

class ChatRepositoryTest {

    private lateinit var repository: ChatRepository
    private val apiMock: OpenRouterApiService = mockk()
    private val testDispatcher = StandardTestDispatcher()

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
        mockkObject(RetrofitInstance)
        every { RetrofitInstance.api } returns apiMock

        repository = ChatRepository()
    }

    @Before
    fun mockAndroidLog() {
        mockkStatic(Log::class)
        every { Log.e(any(), any()) } returns 0
        every { Log.e(any(), any(), any()) } returns 0
    }

    @After
    fun unmock() {
        unmockkAll()
        Dispatchers.resetMain()
    }


    @After
    fun tearDown() {
        Dispatchers.resetMain()
        unmockkAll()
    }

    @Test
    fun `sendMessage returns expected message on success`() = runTest {
        val expectedContent = "Ешь больше овощей"
        val userMessage = "Что мне есть?"

        coEvery {
            apiMock.getChatCompletion(any())
        } returns OpenRouterResponse(
            listOf(Choice(Message("assistant", expectedContent)))
        )

        val result = repository.sendMessage(userMessage)
        testDispatcher.scheduler.advanceUntilIdle()

        assertEquals(expectedContent, result)
    }

    @Test
    fun `sendMessage returns fallback message when response is empty`() = runTest {
        val userMessage = "Что мне есть?"

        coEvery {
            apiMock.getChatCompletion(any())
        } returns OpenRouterResponse(emptyList())

        val result = repository.sendMessage(userMessage)
        testDispatcher.scheduler.advanceUntilIdle()

        assertEquals("Ошибка: пустой ответ", result)
    }

    @Test
    fun `sendMessage handles exception and returns fallback`() = runTest {
        val userMessage = "Что мне есть?"

        coEvery {
            apiMock.getChatCompletion(any())
        } throws RuntimeException("Сетевая ошибка")

        val result = repository.sendMessage(userMessage)
        testDispatcher.scheduler.advanceUntilIdle()

        assertEquals("Советов пока нет", result)
    }
}

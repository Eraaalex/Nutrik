@file:OptIn(kotlinx.coroutines.ExperimentalCoroutinesApi::class)

package com.hse.coursework.nutrik.repository

import com.hse.coursework.nutrik.model.Consumption
import com.hse.coursework.nutrik.model.ProductEntity
import com.hse.coursework.nutrik.repository.consumption.ConsumptionRepositoryImpl
import io.mockk.Runs
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.just
import io.mockk.mockk
import io.mockk.unmockkAll
import kotlinx.coroutines.CoroutineScope
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
import java.time.LocalDate

@OptIn(ExperimentalCoroutinesApi::class)
class ConsumptionRepositoryImplTest {

    private val local: LocalDataSource = mockk(relaxed = true)
    private val remote: RemoteDataSource = mockk(relaxed = true)
    private lateinit var repo: ConsumptionRepositoryImpl
    private val testDispatcher = StandardTestDispatcher()

    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)
        repo = ConsumptionRepositoryImpl(local, remote)
    }

    @After
    fun teardown() {
        Dispatchers.resetMain()
        unmockkAll()
    }

    @Test
    fun `getDatesBetween returns all dates inclusive`() {
        val start = LocalDate.of(2024, 5, 19)
        val end = LocalDate.of(2024, 5, 21)
        val expected = listOf(
            LocalDate.of(2024, 5, 19),
            LocalDate.of(2024, 5, 20),
            LocalDate.of(2024, 5, 21),
        )
        assertEquals(expected, repo.getDatesBetween(start, end))
    }

    @Test
    fun `updateConsumption saves to local if date within 3 days`() = runTest {
        val product = ProductEntity(id = "p", name = "Product")
        val userId = "u"
        val newWeight = 100.0

        coEvery { local.insertConsumption(any()) } just Runs
        coEvery { remote.insertConsumption(any()) } just Runs

        repo.updateConsumption(product, newWeight, userId)
        coVerify { local.insertConsumption(any()) }
        coVerify { remote.insertConsumption(any()) }
    }

    @Test
    fun `updateConsumption always saves to remote`() = runTest {
        val product = ProductEntity(id = "p", name = "Product")
        val userId = "u"
        val newWeight = 100.0

        coEvery { local.insertConsumption(any()) } just Runs
        coEvery { remote.insertConsumption(any()) } just Runs

        repo.updateConsumption(product, newWeight, userId)
        coVerify { remote.insertConsumption(any()) }
    }

    @Test
    fun `getConsumptionForWeek returns local if all dates covered`() = runTest {
        val userId = "user"
        val start = LocalDate.now().minusDays(1)
        val end = LocalDate.now()
        val consList = listOf(
            Consumption("p1", "product", userId, start, 100.0),
            Consumption("p2", "product", userId, end, 150.0)
        )
        coEvery { local.getConsumptionByDateRange(userId, start, end) } returns consList

        val scope = CoroutineScope(testDispatcher)
        val result = repo.getConsumptionForWeek(userId, start, end, scope)
        assertEquals(consList, result)
        coVerify(exactly = 0) { remote.getConsumptionByDates(any(), any()) }
    }

    @Test
    fun `getConsumptionForWeek merges local and remote for missing dates and caches remote only if local empty`() =
        runTest {
            val userId = "user"
            val start = LocalDate.of(2024, 5, 18)
            val end = LocalDate.of(2024, 5, 20)
            val locals = listOf(
                Consumption("p1", "product", userId, start, 100.0)
            )
            val missingDates = listOf(
                LocalDate.of(2024, 5, 19),
                LocalDate.of(2024, 5, 20)
            )
            val remotes = listOf(
                Consumption("p2", "product", userId, missingDates[0], 200.0),
                Consumption("p3", "product", userId, missingDates[1], 300.0)
            )
            coEvery { local.getConsumptionByDateRange(userId, start, end) } returns locals
            coEvery { remote.getConsumptionByDates(userId, missingDates) } returns remotes
            coEvery { local.insertConsumption(any()) } just Runs

            val scope = CoroutineScope(testDispatcher)
            val result = repo.getConsumptionForWeek(userId, start, end, scope)

            assertEquals(locals + remotes, result)
        }

    @Test
    fun `getConsumptionForWeek caches remote only if local is empty`() = runTest {
        val userId = "user"
        val start = LocalDate.of(2024, 5, 18)
        val end = LocalDate.of(2024, 5, 20)
        val locals = emptyList<Consumption>()
        val allDates = listOf(start, start.plusDays(1), end)
        val remotes = listOf(
            Consumption("p1", "product", userId, allDates[0], 100.0),
            Consumption("p2", "product", userId, allDates[1], 200.0),
            Consumption("p3", "product", userId, allDates[2], 300.0)
        )
        coEvery { local.getConsumptionByDateRange(userId, start, end) } returns locals
        coEvery { remote.getConsumptionByDates(userId, allDates) } returns remotes
        coEvery { local.insertConsumption(any()) } just Runs

        val scope = CoroutineScope(testDispatcher)
        repo.getConsumptionForWeek(userId, start, end, scope)
        coVerify(exactly = remotes.size) { local.insertConsumption(any()) }
    }
}

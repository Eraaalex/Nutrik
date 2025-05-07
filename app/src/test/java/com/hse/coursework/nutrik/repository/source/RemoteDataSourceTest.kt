@file:OptIn(kotlinx.coroutines.ExperimentalCoroutinesApi::class)

package com.hse.coursework.nutrik.repository.source

import android.util.Log
import com.google.firebase.firestore.DocumentSnapshot
import com.hse.coursework.nutrik.model.Consumption
import com.hse.coursework.nutrik.model.ConsumptionDTO
import com.hse.coursework.nutrik.model.ProductEntity
import com.hse.coursework.nutrik.model.toConsumption
import com.hse.coursework.nutrik.repository.RemoteDataSource
import com.hse.coursework.nutrik.service.FirebaseService
import com.hse.coursework.nutrik.service.PaginatedResult
import io.mockk.Runs
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.just
import io.mockk.mockk
import io.mockk.mockkStatic
import io.mockk.unmockkAll
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Before
import org.junit.Test
import java.time.LocalDate

@OptIn(ExperimentalCoroutinesApi::class)
class RemoteDataSourceTest {

    private val firebaseService: FirebaseService = mockk()
    private lateinit var remote: RemoteDataSource

    private val testDispatcher = StandardTestDispatcher()

    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)
        mockkStatic(Log::class)
        every { Log.e(any(), any()) } returns 0
        every { Log.d(any(), any()) } returns 0
        remote = RemoteDataSource(firebaseService)
    }

    @After
    fun teardown() {
        unmockkAll()
        Dispatchers.resetMain()
    }

    @Test
    fun `searchProducts emits results from firebaseService`() = runTest {
        val products = listOf(ProductEntity(id = "1"), ProductEntity(id = "2"))
        coEvery { firebaseService.searchProducts("apple") } returns products

        val result = remote.searchProducts("apple").first()
        assertEquals(products, result)
    }

    @Test
    fun `fetchProductData emits paginated result from firebaseService`() = runTest {
        val doc: DocumentSnapshot = mockk()
        val paginated = PaginatedResult(listOf(ProductEntity("1")), null)
        coEvery { firebaseService.getProductData(10, doc, "milk") } returns paginated

        val result = remote.fetchProductData(10, doc, "milk").first()
        assertEquals(paginated, result)
    }

    @Test
    fun `getById emits product from firebaseService`() = runTest {
        val product = ProductEntity("id")
        coEvery { firebaseService.getProductById("id") } returns product

        val result = remote.getById("id").first()
        assertEquals(product, result)
    }

    @Test
    fun `updateFavorites calls firebaseService updateUserFavorites`() = runTest {
        coEvery { firebaseService.updateUserFavorites("u", listOf("1", "2")) } just Runs

        remote.updateFavorites("u", listOf("1", "2"))
        coVerify { firebaseService.updateUserFavorites("u", listOf("1", "2")) }
    }

    @Test
    fun `fetchUserFavorites returns user favorites`() = runTest {
        val ids = listOf("a", "b")
        coEvery { firebaseService.getUserFavorites("u") } returns ids

        val result = remote.fetchUserFavorites("u")
        assertEquals(ids, result)
    }

    @Test
    fun `insertConsumption delegates to firebaseService`() = runTest {
        val cons = Consumption("id", "productName", "userId", LocalDate.now(), 10.0)

        coEvery { firebaseService.insertConsumption(any()) } just Runs

        remote.insertConsumption(cons)
        coVerify { firebaseService.insertConsumption(any()) }
    }

    @Test
    fun `getByName emits product from firebaseService`() = runTest {
        val product = ProductEntity("1", "000", "Milk", "milk")
        coEvery { firebaseService.getProductByName("milk") } returns product

        val result = remote.getByName("milk").first()
        assertEquals(product, result)
    }

    @Test
    fun `getByName emits null when firebaseService throws`() = runTest {
        coEvery { firebaseService.getProductByName("unknown") } throws RuntimeException("not found")

        val result = remote.getByName("unknown").first()
        assertNull(result)
    }

    @Test
    fun `getConsumptionByDates maps snapshot to Consumption`() = runTest {
        val snapshot = listOf(
            ConsumptionDTO("id", "productName", "userId", LocalDate.now().toString(), 10.0),
            ConsumptionDTO("id1", "productName1", "userId1", LocalDate.now().toString(), 100.0)
        )
        val cons1 = snapshot[0].toConsumption()
        val cons2 = snapshot[1].toConsumption()

        coEvery {
            firebaseService.getConsumptionByDates(
                "u",
                listOf(LocalDate.of(2024, 1, 1))
            )
        } returns snapshot


        val result = remote.getConsumptionByDates("u", listOf(LocalDate.of(2024, 1, 1)))
        assertEquals(listOf(cons1, cons2), result)
    }
}

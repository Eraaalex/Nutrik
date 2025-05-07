package com.hse.coursework.nutrik.viewmodel

import android.util.Log
import com.hse.coursework.nutrik.model.ProductEntity
import com.hse.coursework.nutrik.model.Restriction
import com.hse.coursework.nutrik.repository.LocalDataSource
import com.hse.coursework.nutrik.repository.RemoteDataSource
import com.hse.coursework.nutrik.repository.user.UserRepository
import com.hse.coursework.nutrik.service.PaginatedResult
import com.hse.coursework.nutrik.ui.theme.screen.search.SearchViewModel
import io.mockk.coEvery
import io.mockk.every
import io.mockk.mockk
import io.mockk.mockkStatic
import io.mockk.unmockkAll
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class SearchViewModelTest {

    private val local: LocalDataSource = mockk()
    private val remote: RemoteDataSource = mockk()
    private val userRepo: UserRepository = mockk()

    private lateinit var vm: SearchViewModel

    private val testDispatcher = StandardTestDispatcher()

    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)
        vm = SearchViewModel(local, remote, userRepo)
    }

    @Before
    fun mockLog() {
        mockkStatic(Log::class)
        every { Log.e(any(), any()) } returns 0
    }


    @Test
    fun `loadProducts adds local and remote data and merges`() = runTest {
        val localList = listOf(ProductEntity(id = "1", name = "local"))
        val remoteList = listOf(ProductEntity(id = "2", name = "remote"))

        coEvery { local.searchProducts(any(), any(), any()) } returns localList
        coEvery {
            remote.fetchProductData(any(), any(), any())
        } returns flowOf(PaginatedResult(data = remoteList, lastDocument = null))
        coEvery { local.saveProductData(any()) } returns Unit

        vm.loadProducts(reset = true)
        advanceUntilIdle()

        val result = vm.productList.value
        assertEquals(2, result.size)
        assertTrue(result.any { it.id == "1" })
        assertTrue(result.any { it.id == "2" })
    }

    @Test
    fun `updateSearchQuery with query triggers full search`() = runTest {
        val localHits = listOf(ProductEntity(id = "1", name = "milk"))
        val remoteHits = listOf(ProductEntity(id = "2", name = "milk"))

        coEvery {
            local.searchProducts(query = "milk", limit = Int.MAX_VALUE, offset = 0)
        } returns localHits

        coEvery { remote.searchProducts("milk") } returns flowOf(remoteHits)
        coEvery { local.saveProductData(any()) } returns Unit

        vm.updateSearchQuery("milk")
        advanceUntilIdle()

        val result = vm.productList.value
        assertEquals(2, result.size)
    }

    @Test
    fun `updateSearchQuery with empty string loads alphabetically`() = runTest {
        val alphaList = listOf(ProductEntity(id = "1", name = "apple"))
        coEvery { local.getRecentProducts(any(), any()) } returns alphaList
        coEvery {
            remote.fetchProductData(any(), any(), null)
        } returns flowOf(PaginatedResult(data = emptyList(), lastDocument = null))
        coEvery { local.saveProductData(any()) } returns Unit

        vm.updateSearchQuery("")
        advanceUntilIdle()

        val result = vm.productList.value
        assertEquals(1, result.size)
        assertEquals("apple", result.first().name)
    }

    @Test
    fun `isForbidden returns true if restriction matches allergens`() = runTest {
        val restriction = Restriction.LACTOSE
        val product = ProductEntity(id = "1", allergens = setOf("lactose"))
        coEvery { userRepo.getUser() } returns mockk {
            every { restrictions } returns listOf(restriction)
        }

        vm.fetchUserRestrictions()
        advanceUntilIdle()

        val result = vm.isForbidden(product)
        assertTrue(result)
    }

    @Test
    fun `isForbidden returns false if no matching restriction`() = runTest {
        val restriction = Restriction.LACTOSE
        val product = ProductEntity(id = "1", allergens = setOf("gluten"))
        coEvery { userRepo.getUser() } returns mockk {
            every { restrictions } returns listOf(restriction)
        }

        vm.fetchUserRestrictions()
        advanceUntilIdle()

        val result = vm.isForbidden(product)
        assertFalse(result)
    }

    @After
    fun tearDown() {
        unmockkAll()
        Dispatchers.resetMain()
    }
}

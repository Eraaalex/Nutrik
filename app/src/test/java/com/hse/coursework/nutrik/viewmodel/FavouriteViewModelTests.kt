@file:OptIn(ExperimentalCoroutinesApi::class)

package com.hse.coursework.nutrik.viewmodel

import android.util.Log
import app.cash.turbine.test
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.hse.coursework.nutrik.model.ProductEntity
import com.hse.coursework.nutrik.repository.product.ProductRepository
import com.hse.coursework.nutrik.ui.theme.screen.favourite.FavouriteViewModel
import io.mockk.coEvery
import io.mockk.coVerify
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
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class FavouriteViewModelTest {

    private val repository: ProductRepository = mockk()
    private val firebaseAuth: FirebaseAuth = mockk()
    private val firebaseUser: FirebaseUser = mockk()
    private lateinit var viewModel: FavouriteViewModel

    private val testDispatcher = StandardTestDispatcher()

    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)
        mockkStatic(Log::class)
        every { Log.e(any(), any()) } returns 0

        every { firebaseAuth.currentUser } returns firebaseUser
        every { firebaseUser.uid } returns "user123"

        coEvery { repository.getFavoriteProducts() } returns flowOf(emptyList())
        viewModel = FavouriteViewModel(repository, firebaseAuth)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
        unmockkAll()
    }

    @Test
    fun `init collects favorite products into productList`() = runTest {
        val favorites = listOf(ProductEntity(id = "1"), ProductEntity(id = "2"))
        coEvery { repository.getFavoriteProducts() } returns flowOf(favorites)

        val vm = FavouriteViewModel(repository, firebaseAuth)
        advanceUntilIdle()

        assertEquals(favorites, vm.productList.value)
    }

    @Test
    fun `loadProducts fetches and paginates favorites`() = runTest {
        val favorites = listOf(ProductEntity(id = "1"), ProductEntity(id = "2"))
        coEvery { repository.fetchAllFavoriteIds("user123") } returns listOf("1", "2")
        coEvery {
            repository.fetchFavoritesByPage(listOf("1", "2"), 10, 0)
        } returns flowOf(favorites)

        viewModel.loadProducts()
        advanceUntilIdle()

        assertEquals(favorites, viewModel.productList.value)
    }

    @Test
    fun `isLoading toggles true then false during load`() = runTest {
        coEvery { repository.fetchAllFavoriteIds("user123") } returns listOf()
        coEvery {
            repository.fetchFavoritesByPage(any(), any(), any())
        } returns flowOf(emptyList())

        viewModel.isLoading.test {
            assertEquals(false, awaitItem())
            viewModel.loadProducts()
            assertEquals(true, awaitItem())
            advanceUntilIdle()
            assertEquals(false, awaitItem())
        }
    }

    @Test
    fun `loadProducts caches favoriteIds after first call`() = runTest {
        val ids = listOf("1", "2")
        val page1 = listOf(ProductEntity(id = "1"))
        val page2 = listOf(ProductEntity(id = "2"))

        coEvery { repository.fetchAllFavoriteIds("user123") } returns ids
        coEvery { repository.fetchFavoritesByPage(ids, 10, 0) } returns flowOf(page1)
        coEvery { repository.fetchFavoritesByPage(ids, 10, 1) } returns flowOf(page2)

        viewModel.loadProducts()
        advanceUntilIdle()
        viewModel.loadProducts()
        advanceUntilIdle()

        assertEquals(page1 + page2, viewModel.productList.value)
        coVerify(exactly = 1) { repository.fetchAllFavoriteIds("user123") }
    }

    @Test
    fun `loadProducts catches exception and sets isLoading to false`() = runTest {
        coEvery { repository.fetchAllFavoriteIds(any()) } throws RuntimeException("Fail!")

        viewModel.loadProducts()
        advanceUntilIdle()

        assertEquals(false, viewModel.isLoading.value)
    }

}

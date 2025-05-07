@file:OptIn(ExperimentalCoroutinesApi::class)

package com.hse.coursework.nutrik.repository

import android.content.res.Resources
import android.util.Log
import com.hse.coursework.nutrik.model.ProductEntity
import com.hse.coursework.nutrik.repository.product.ProductRepository
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
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class ProductRepositoryTest {

    private val local: LocalDataSource = mockk(relaxed = true)
    private val remote: RemoteDataSource = mockk(relaxed = true)
    private lateinit var repo: ProductRepository

    private val testDispatcher = StandardTestDispatcher()

    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)
        mockkStatic(Log::class)
        every { Log.e(any(), any()) } returns 0
        every { Log.d(any(), any()) } returns 0
        every { Log.e(any(), any(), any()) } returns 0

        repo = ProductRepository(local, remote)
    }

    @Before
    fun mockAndroidLog() {
        mockkStatic(Log::class)
        every { Log.e(any(), any()) } returns 0
        every { Log.e(any(), any(), any()) } returns 0
        every { Log.d(any(), any()) } returns 0
    }

    @After
    fun unmock() {
        unmockkAll()
        Dispatchers.resetMain()
    }


    @After
    fun teardown() {
        Dispatchers.resetMain()
        unmockkAll()
    }

    @Test
    fun `getById returns local product if available`() = runTest {
        val id = "123"
        val localProduct = ProductEntity("testId", "Remote", "category", "testName", "g", 1.0, 1.0)

        every { local.getById(id) } returns flowOf(localProduct)

        val result = repo.getById(id).first()
        assertEquals(localProduct, result)
    }

    @Test
    fun `getById fetches from remote and saves if local is null`() = runTest {
        val id = "123"
        val remoteProduct = ProductEntity(id, "Remote", "category", "testName", "g", 1.0, 1.0)

        every { local.getById(id) } returns flowOf(null)
        every { remote.getById(id) } returns flowOf(remoteProduct)
        coEvery { local.saveProduct(remoteProduct) } just Runs

        val result = repo.getById(id).first()
        assertEquals(remoteProduct, result)
        coVerify { local.saveProduct(remoteProduct) }
    }

    @Test
    fun `toggleFavorite adds or removes favorite depending on current state`() = runTest {
        val id = "123"
        val product = ProductEntity(id, "Remote", "category", "testName", "g", 1.0, 1.0)

        val userId = "user"

        every { local.isFavorite(product.id) } returns flowOf(true)
        coEvery { local.removeFavorite(product.id) } just Runs
        coEvery { local.getAllFavoriteIds() } returns flowOf(listOf())
        coEvery { remote.updateFavorites(userId, any()) } just Runs

        repo.toggleFavorite(product, userId)

        coVerify { local.removeFavorite(product.id) }
        coVerify { remote.updateFavorites(userId, any()) }
    }

    @Test
    fun `getProductByName emits local if found`() = runTest {
        val name = "Banana"
        val localProduct = ProductEntity("testId", "Remote", "category", name, "g", 1.0, 1.0)


        every { local.getByName(name) } returns flowOf(localProduct)

        val result = repo.getProductByName(name).first()
        assertEquals(localProduct, result)
    }

    @Test
    fun `getProductByName fetches remote if not in local`() = runTest {
        val name = "Banana"
        val remoteProduct = ProductEntity("testId", "Remote", "category", name, "g", 1.0, 1.0)

        every { local.getByName(name) } returns flowOf(null)
        every { remote.getByName(name) } returns flowOf(remoteProduct)
        coEvery { local.saveProduct(remoteProduct) } just Runs

        val result = repo.getProductByName(name).first()
        assertEquals(remoteProduct, result)
        coVerify { local.saveProduct(remoteProduct) }
    }

    @Test
    fun `getProductByBarcode returns local if available`() = runTest {
        val id = "123"
        val localProduct = ProductEntity(id, "src", "cat", "name", "g", 1.0, 1.0)

        every { local.getById(id) } returns flowOf(localProduct)

        val result = repo.getProductByBarcode(id).first()
        assertEquals(localProduct, result)
    }

    @Test
    fun `getProductByBarcode fetches remote when local is null`() = runTest {
        val id = "123"
        val remoteProduct = ProductEntity(id, "src", "cat", "name", "g", 1.0, 1.0)

        every { local.getById(id) } returns flowOf(null)
        every { remote.getById(id) } returns flowOf(remoteProduct)
        coEvery { local.saveProduct(remoteProduct) } just Runs

        val result = repo.getProductByBarcode(id).first()
        assertEquals(remoteProduct, result)
        coVerify { local.saveProduct(remoteProduct) }
    }

    @Test
    fun `fetchFavoritesByPage returns empty when page out of range`() = runTest {
        val ids = listOf("a", "b", "c")
        val result = repo.fetchFavoritesByPage(ids, pageSize = 2, pageIndex = 2).first()
        assertTrue(result.isEmpty())
    }

    @Test
    fun `fetchFavoritesByPage returns page of products with local and remote fallback`() = runTest {
        val ids = listOf("p1", "p2", "p3")
        val prod1 = ProductEntity("p1", "src", "cat", "n1", "g", 1.0, 1.0)
        val prod2 = ProductEntity("p2", "src", "cat", "n2", "g", 2.0, 2.0)

        every { local.getById("p1") } returns flowOf(prod1)
        every { local.getById("p2") } returns flowOf(null)
        every { remote.getById("p2") } returns flowOf(prod2)
        coEvery { local.saveProduct(prod2) } just Runs

        val result = repo.fetchFavoritesByPage(ids, pageSize = 2, pageIndex = 0).first()
        assertEquals(listOf(prod1, prod2), result)
        coVerify { local.saveProduct(prod2) }
    }

    @Test
    fun `fetchFavoritesFromRemote saves each fetched favorite`() = runTest {
        val userId = "user"
        val fetched = listOf("x", "y")

        coEvery { remote.fetchUserFavorites(userId) } returns fetched
        coEvery { local.saveFavorite(any()) } just Runs

        repo.fetchFavoritesFromRemote(userId)

        coVerify { local.saveFavorite("x") }
        coVerify { local.saveFavorite("y") }
    }

    @Test
    fun `fetchAllFavoriteIds returns local when available`() = runTest {
        val userId = "user"
        every { local.getAllFavoriteIds() } returns flowOf(listOf("a", "b"))

        val result = repo.fetchAllFavoriteIds(userId)
        assertEquals(listOf("a", "b"), result)
    }

    @Test
    fun `fetchAllFavoriteIds fetches remote when local null`() = runTest {
        val userId = "user"
        every { local.getAllFavoriteIds() } returns flowOf(listOf("c"))
        coEvery { remote.fetchUserFavorites(userId) } returns listOf("b")

        val result = repo.fetchAllFavoriteIds(userId)
        assertEquals(listOf("c"), result)
    }

    @Test
    fun `getFavoriteProducts returns flow from local`() = runTest {
        val prods = listOf(ProductEntity("id", "src", "cat", "n", "g", 0.0, 0.0))
        every { local.getFavoriteProducts() } returns flowOf(prods)

        val result = repo.getFavoriteProducts().first()
        assertEquals(prods, result)
    }

    @Test
    fun `getProductByName emits null when not found remotely`() = runTest {
        val name = "None"
        every { local.getByName(name) } returns flowOf(null)
        every { remote.getByName(name) } returns flowOf(null)

        val result = repo.getProductByName(name).firstOrNull()
        assertNull(result)
    }

    @Test
    fun `getProductByName catches NotFoundException and emits nothing`() = runTest {
        val name = "x"
        every { local.getByName(name) } returns flowOf(null)
        coEvery { remote.getByName(name) } throws Resources.NotFoundException()

        val flow = repo.getProductByName(name)
        assertNull(flow.firstOrNull())
    }
}

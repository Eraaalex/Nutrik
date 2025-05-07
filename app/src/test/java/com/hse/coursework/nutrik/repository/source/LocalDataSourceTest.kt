@file:OptIn(kotlinx.coroutines.ExperimentalCoroutinesApi::class)

package com.hse.coursework.nutrik.repository.source

import com.hse.coursework.nutrik.data.dao.ConsumeDao
import com.hse.coursework.nutrik.data.dao.FavoriteDao
import com.hse.coursework.nutrik.data.dao.ProductDao
import com.hse.coursework.nutrik.data.dao.ProgressDao
import com.hse.coursework.nutrik.model.Consumption
import com.hse.coursework.nutrik.model.ConsumptionEntity
import com.hse.coursework.nutrik.model.FavoriteEntity
import com.hse.coursework.nutrik.model.ProductEntity
import com.hse.coursework.nutrik.repository.LocalDataSource
import io.mockk.Runs
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.just
import io.mockk.mockk
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import java.time.LocalDate

@OptIn(ExperimentalCoroutinesApi::class)
class LocalDataSourceTest {

    private val productDao: ProductDao = mockk()
    private val favoriteDao: FavoriteDao = mockk()
    private val progressDao: ProgressDao = mockk()
    private val consumeDao: ConsumeDao = mockk()

    private lateinit var dataSource: LocalDataSource
    private val testDispatcher = StandardTestDispatcher()

    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)
        dataSource = LocalDataSource(progressDao, productDao, favoriteDao, consumeDao)
    }

    @After
    fun teardown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `getRecentProducts returns expected list`() = runTest {
        val expected = listOf(ProductEntity(id = "1", name = "Milk"))
        coEvery { productDao.getRecentProducts(5, 0) } returns expected

        val result = dataSource.getRecentProducts(5, 0)
        assertEquals(expected, result)
    }

    @Test
    fun `searchProducts returns filtered products when query is not blank`() = runTest {
        val dbResult = listOf(
            ProductEntity(id = "1", name = "Apple"),
            ProductEntity(id = "2", name = "Apricot")
        )
        coEvery { productDao.searchByName("%Ap%") } returns dbResult

        val result = dataSource.searchProducts("Ap", 1, 1) // drop 1, take 1
        assertEquals(listOf(ProductEntity(id = "2", name = "Apricot")), result)
    }

    @Test
    fun `searchProducts returns empty list for blank query`() = runTest {
        val result = dataSource.searchProducts("   ", 5, 0)
        assertTrue(result.isEmpty())
    }

    @Test
    fun `saveProductData calls productDao insertAll`() = runTest {
        val products = listOf(ProductEntity(id = "1"), ProductEntity(id = "2"))
        coEvery { productDao.insertAll(products) } just Runs
        dataSource.saveProductData(products)
        coVerify { productDao.insertAll(products) }
    }

    @Test
    fun `getById emits product from dao`() = runTest {
        val prod = ProductEntity(id = "1", name = "Test")
        coEvery { productDao.getById("1") } returns prod


        val flow = dataSource.getById("1")
        flow.collect { result ->
            assertEquals(prod, result)
        }
    }

    @Test
    fun `saveProduct does nothing on null`() = runTest {
        dataSource.saveProduct(null)
        coVerify(exactly = 0) { productDao.insertProduct(any()) }
    }

    @Test
    fun `saveProduct calls insertProduct on non-null`() = runTest {
        val product = ProductEntity(id = "1")
        coEvery { productDao.insertProduct(product) } just Runs
        dataSource.saveProduct(product)
        coVerify { productDao.insertProduct(product) }
    }

    @Test
    fun `saveFavorite calls insertFavorite`() = runTest {
        coEvery { favoriteDao.insertFavorite(FavoriteEntity("p1")) } just Runs
        dataSource.saveFavorite("p1")
        coVerify { favoriteDao.insertFavorite(FavoriteEntity("p1")) }
    }

    @Test
    fun `removeFavorite calls removeFavorite`() = runTest {
        coEvery { favoriteDao.removeFavorite(FavoriteEntity("p1")) } just Runs
        dataSource.removeFavorite("p1")
        coVerify { favoriteDao.removeFavorite(FavoriteEntity("p1")) }
    }

    @Test
    fun `isFavorite emits false if count is zero`() = runTest {
        val flow = flowOf(0)
        every { favoriteDao.isFavorite("p2") } returns flow

        val isFavoriteFlow = dataSource.isFavorite("p2")
        isFavoriteFlow.collect { result -> assertFalse(result) }
    }

    @Test
    fun `getAllFavoriteIds emits dao value`() = runTest {
        val ids = listOf("1", "2")
        val flow = flowOf(ids)
        every { favoriteDao.getAllFavoriteIds() } returns flow

        val result = dataSource.getAllFavoriteIds().first()
        assertEquals(ids, result)
    }

    @Test
    fun `getFavoriteProducts emits correct products`() = runTest {
        val ids = listOf("1", "2")
        val products = listOf(ProductEntity(id = "1"), ProductEntity(id = "2"))
        val idsFlow = flowOf(ids)
        val productsFlow = flowOf(products)

        every { favoriteDao.getAllFavoriteIds() } returns idsFlow
        every { productDao.getProductsByIds(ids) } returns productsFlow

        val result = dataSource.getFavoriteProducts().first()
        assertEquals(products, result)
    }

    @Test
    fun `insertConsumption calls consumeDao insert`() = runTest {
        val cons = Consumption("id", "user", "dd", LocalDate.now(), 100.0)
        coEvery { consumeDao.insertConsumption(any()) } just Runs
        dataSource.insertConsumption(cons)
        coVerify { consumeDao.insertConsumption(any()) }
    }

    @Test
    fun `getByName emits dao product`() = runTest {
        val prod = ProductEntity(id = "99")
        every { productDao.getByName("Milk") } returns prod
        val flow = dataSource.getByName("Milk")
        flow.collect { result -> assertEquals(prod, result) }
    }

    @Test
    fun `getConsumptionByDate returns mapped list`() = runTest {
        val consEntity =
            ConsumptionEntity(0L, "id", "productName", "userId", LocalDate.now().toString(), 10.0)
        val cons = Consumption("id", "productName", "userId", LocalDate.now(), 10.0)
        coEvery { consumeDao.getConsumptionByDate("u", any()) } returns listOf(consEntity)
        val result = dataSource.getConsumptionByDate("u", LocalDate.now())
        assertEquals(listOf(cons), result)
    }

    @Test
    fun `getConsumptionByDateRange returns mapped list`() = runTest {
        val consEntity =
            ConsumptionEntity(0L, "id", "productName", "userId", LocalDate.now().toString(), 10.0)
        val cons = Consumption("id", "productName", "userId", LocalDate.now(), 10.0)

        coEvery { consumeDao.getConsumptionByDateRange("u", any(), any()) } returns listOf(
            consEntity
        )
        val result = dataSource.getConsumptionByDateRange("u", LocalDate.now(), LocalDate.now())
        assertEquals(listOf(cons), result)
    }
}
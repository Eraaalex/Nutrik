package com.hse.coursework.nutrik.repository

import com.hse.coursework.nutrik.model.Consumption
import com.hse.coursework.nutrik.model.FavoriteEntity
import com.hse.coursework.nutrik.model.Product
import com.hse.coursework.nutrik.model.ProgressItem
import com.hse.coursework.nutrik.model.dao.ConsumeDao
import com.hse.coursework.nutrik.model.dao.FavoriteDao
import com.hse.coursework.nutrik.model.dao.ProductDao
import com.hse.coursework.nutrik.model.dao.ProgressDao
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class LocalDataSource @Inject constructor(
    private val progressDao: ProgressDao,
    private val productDao: ProductDao,
    private val favoriteDao: FavoriteDao,
    private val consumeDao : ConsumeDao
) {

    fun getProgressData(): Flow<List<ProgressItem>> {
        return progressDao.getAllProgressItems()
    }

    suspend fun saveProgressData(data: List<ProgressItem>) {
        progressDao.insertAll(data)
    }

    suspend fun searchProducts(query: String, limit: Int, offset: Int): List<Product> {
        val searchQuery = if (query.isBlank()) null else "%$query%"
        return productDao.searchProducts(searchQuery, limit, offset)
    }

    fun getById(id: String): Flow<Product?> =flow {
        productDao.getById(id)
    }

    suspend fun save(remoteData: Flow<Product>) {
        productDao.insert(remoteData.first())
    }

    suspend fun saveProduct(product: Product) {
        productDao.insertProduct(product)
    }

    suspend fun saveFavorite(productId: String) {
        favoriteDao.insertFavorite(FavoriteEntity(productId))
    }

    suspend fun removeFavorite(productId: String) {
        favoriteDao.removeFavorite(FavoriteEntity(productId))
    }

    fun isFavorite(productId: String): Flow<Boolean> {
        return favoriteDao.isFavorite(productId).map { count -> count > 0 }
    }

    fun getAllFavoriteIds(): Flow<List<String>> {
        return favoriteDao.getAllFavoriteIds()
    }

    suspend fun saveProductData(products: List<Product>) {
        productDao.insertAll(products)
    }

    fun getFavoriteProducts(): Flow<List<Product>> {
        return favoriteDao.getAllFavoriteIds().flatMapLatest { ids ->
            productDao.getProductsByIds(ids)
        }
    }

    fun insertConsumption(consumption: Consumption) {
        // TODO : Implement this method
    }

}

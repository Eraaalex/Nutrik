package com.hse.coursework.nutrik.repository

import android.util.Log
import com.hse.coursework.nutrik.data.dao.ConsumeDao
import com.hse.coursework.nutrik.data.dao.FavoriteDao
import com.hse.coursework.nutrik.data.dao.ProductDao
import com.hse.coursework.nutrik.data.dao.ProgressDao
import com.hse.coursework.nutrik.model.Consumption
import com.hse.coursework.nutrik.model.FavoriteEntity
import com.hse.coursework.nutrik.model.ProductEntity
import com.hse.coursework.nutrik.model.toConsumption
import com.hse.coursework.nutrik.model.toEntity
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map
import java.time.LocalDate
import javax.inject.Inject

class LocalDataSource @Inject constructor(
    private val progressDao: ProgressDao,
    private val productDao: ProductDao,
    private val favoriteDao: FavoriteDao,
    private val consumeDao: ConsumeDao
) {

    suspend fun getRecentProducts(limit: Int, offset: Int): List<ProductEntity> =
        productDao.getRecentProducts(limit, offset)

    suspend fun searchProducts(query: String, limit: Int, offset: Int): List<ProductEntity> {
        if (query.isBlank()) return emptyList()
        // LIKE '%query%'
        val pattern = "%${query.trim()}%"
        return productDao.searchByName(pattern)
            .drop(offset)
            .take(limit)
    }

    suspend fun saveProductData(products: List<ProductEntity>) {
        productDao.insertAll(products)
    }

    fun getById(id: String): Flow<ProductEntity?> = flow {
        productDao.getById(id)
    }


    suspend fun saveProduct(product: ProductEntity?) {
        if (product == null) return
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


    fun getFavoriteProducts(): Flow<List<ProductEntity>> {
        return favoriteDao.getAllFavoriteIds().flatMapLatest { ids ->
            productDao.getProductsByIds(ids)
        }
    }

    suspend fun insertConsumption(consumption: Consumption) {
        consumeDao.insertConsumption(consumption.toEntity())
    }

    suspend fun smartInsertConsumption(consumption: Consumption) {
        val oldConsumption = consumeDao.getConsumptionByUserAndDate(
            consumption.userId,
            consumption.date.toString(),
            consumption.productId
        )
        Log.e("LocalDataSource", "Smart insert: $oldConsumption")
        if (oldConsumption != null) {
            consumeDao.insertConsumption(
                oldConsumption.copy(
                    weight = oldConsumption.weight + consumption.weight,
                )
            )
            Log.e("LocalDataSource", "Smart insert updated consumption")
            return
        } else {
            consumeDao.insertConsumption(consumption.toEntity())

        }
     }
    fun getByName(name: String): Flow<ProductEntity?> = flow {
        productDao.getByName(name)
    }

    suspend fun getConsumptionByDate(userId: String, date: LocalDate): List<Consumption> {
        return consumeDao.getConsumptionByDate(userId, date.toString()).map { it.toConsumption() }
    }

    suspend fun getConsumptionByDateRange(
        userId: String,
        start: LocalDate,
        end: LocalDate
    ): List<Consumption> {
        return consumeDao.getConsumptionByDateRange(userId, start.toString(), end.toString())
            .map { it.toConsumption() }
    }

    suspend fun deleteConsumption(entry: Consumption) {
        consumeDao.deleteConsumption(entry.toEntity())
    }

}

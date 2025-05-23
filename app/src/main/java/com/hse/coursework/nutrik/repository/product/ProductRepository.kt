package com.hse.coursework.nutrik.repository.product

import android.content.res.Resources
import android.util.Log
import com.hse.coursework.nutrik.model.ProductEntity
import com.hse.coursework.nutrik.repository.LocalDataSource
import com.hse.coursework.nutrik.repository.RemoteDataSource
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.emitAll
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOf
import javax.inject.Inject

class ProductRepository @Inject constructor(
    private val localDataSource: LocalDataSource,
    private val remoteDataSource: RemoteDataSource
) {

    fun getById(id: String): Flow<ProductEntity?> = flow {
        val localProduct = localDataSource.getById(id).firstOrNull()
        if (localProduct != null) {
            emit(localProduct)
        } else {
            val remoteProduct = remoteDataSource.getById(id).firstOrNull()
            Log.e("ProductRepository", "Remote product loaded: $remoteProduct")
            localDataSource.saveProduct(remoteProduct)
            emit(remoteProduct)
        }
    }

    fun getProductByBarcode(id: String): Flow<ProductEntity?> = flow {
        val localProduct = localDataSource.getById(id).firstOrNull()
        if (localProduct != null) {
            emit(localProduct)
        } else {
            val remoteProduct = remoteDataSource.getById(id).first()
            Log.e("ProductRepository", "Remote product loaded: $remoteProduct")

            localDataSource.saveProduct(remoteProduct)
            emit(remoteProduct)
        }
    }

    suspend fun toggleFavorite(product: ProductEntity, userId: String) {
        if (userId.isBlank()) {
            return
        }
        val isCurrentlyFav = localDataSource.isFavorite(product.id).first()
        if (isCurrentlyFav) {
            localDataSource.removeFavorite(product.id)
        } else {
            localDataSource.saveFavorite(product.id)
        }
        syncFavorites(userId)
    }

    private suspend fun syncFavorites(userId: String) {
        val localFavorites = localDataSource.getAllFavoriteIds().first()
        remoteDataSource.updateFavorites(userId, localFavorites)
    }

    suspend fun fetchFavoritesFromRemote(userId: String) {
        val remoteIds = remoteDataSource.fetchUserFavorites(userId)
        remoteIds.forEach { id ->
            localDataSource.saveFavorite(id)
        }
    }

    fun isFavorite(productId: String): Flow<Boolean> {
        return localDataSource.isFavorite(productId)
    }

    suspend fun fetchAllFavoriteIds(userId: String): List<String> {
        return localDataSource.getAllFavoriteIds().firstOrNull()
            ?: remoteDataSource.fetchUserFavorites(userId)
    }

    fun fetchFavoritesByPage(
        favoriteIds: List<String>,
        pageSize: Int,
        pageIndex: Int
    ): Flow<List<ProductEntity>> = flow {
        val start = pageIndex * pageSize
        val end = minOf(start + pageSize, favoriteIds.size)
        if (start >= end) {
            emit(emptyList())
            return@flow
        }

        val pageIds = favoriteIds.subList(start, end)
        val products = pageIds.mapNotNull { id ->
            localDataSource.getById(id).firstOrNull() ?: remoteDataSource.getById(id).firstOrNull()
                ?.also {
                    localDataSource.saveProduct(it)
                }
        }
        emit(products)
    }

    fun getFavoriteProducts(): Flow<List<ProductEntity>> {
        return localDataSource.getFavoriteProducts()
    }

    fun getProductByName(name: String): Flow<ProductEntity?> = flow {
        localDataSource.getByName(name).firstOrNull()?.let {
            emit(it)
            return@flow
        }

        try {
            val remoteProduct = remoteDataSource.getByName(name).first()
            if (remoteProduct != null) {
                localDataSource.saveProduct(remoteProduct)
                emit(remoteProduct)
            } else {
                Log.d("ProductRepository", "Product not found in remote data source")
                emit(null)
            }
        } catch (e: Exception) {
            Log.e("ProductRepository", "Error loading product", e)
        }
    }.catch { e ->
        // Централизованная обработка ошибок
        when (e) {
            is Resources.NotFoundException -> emitAll(flowOf()) // или специальная обработка
            else -> throw e
        }
    }
}

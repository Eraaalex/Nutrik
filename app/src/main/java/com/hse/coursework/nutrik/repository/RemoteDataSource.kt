package com.hse.coursework.nutrik.repository

import android.util.Log
import com.google.firebase.firestore.DocumentSnapshot
import com.hse.coursework.nutrik.model.Consumption
import com.hse.coursework.nutrik.model.Product
import com.hse.coursework.nutrik.model.ProgressItem
import com.hse.coursework.nutrik.model.toDTO
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import javax.inject.Inject

class RemoteDataSource @Inject constructor(private val firebaseService: FirebaseService) {

    fun fetchProgressData(): Flow<List<ProgressItem>> = flow {
        Log.e("RemoteDataSource", "fetchProductData")
        val data = firebaseService.getProgressData()
        emit(data)
    }

    fun fetchProductData(
        limit: Int,
        startAfter: DocumentSnapshot? = null,
        query: String? = null
    ): Flow<PaginatedResult<Product>> = flow {
        Log.d("RemoteDataSource", "Fetching products from Firebase")
        val result = firebaseService.getProductData(limit, startAfter, query)
        emit(result)
    }

    fun getById(id: String): Flow<Product> = flow {
        Log.d("RemoteDataSource", "Fetching product by id from Firebase")
        val product = firebaseService.getProductById(id)
        emit(product)
    }

    suspend fun updateFavorites(userId: String, favoriteIds: List<String>) {
        firebaseService.updateUserFavorites(userId, favoriteIds)
    }

    suspend fun fetchUserFavorites(userId: String): List<String> {
        Log.e("RemoteDataSource", "Fetching user favorites $userId")
        return firebaseService.getUserFavorites(userId)
    }

    suspend fun insertConsumption(consumption: Consumption) {
        firebaseService.insertConsumption(consumption.toDTO())
    }

}

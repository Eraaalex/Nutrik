package com.hse.coursework.nutrik.repository

import android.util.Log
import com.google.firebase.firestore.DocumentSnapshot
import com.hse.coursework.nutrik.model.Consumption
import com.hse.coursework.nutrik.model.ProductEntity
import com.hse.coursework.nutrik.model.toConsumption
import com.hse.coursework.nutrik.model.dto.toDTO
import com.hse.coursework.nutrik.model.toDTO
import com.hse.coursework.nutrik.service.FirebaseService
import com.hse.coursework.nutrik.service.PaginatedResult
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import java.time.LocalDate
import javax.inject.Inject

class RemoteDataSource @Inject constructor(private val firebaseService: FirebaseService) {

    fun searchProducts(query: String): Flow<List<ProductEntity>> = flow {
        val results = firebaseService.searchProducts(query)
        emit(results)
    }

    fun fetchProductData(
        limit: Int,
        startAfter: DocumentSnapshot? = null,
        query: String? = null
    ): Flow<PaginatedResult<ProductEntity>> = flow {
        emit(firebaseService.getProductData(limit, startAfter, query))
    }


    fun getById(id: String): Flow<ProductEntity?> = flow {
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

    fun getByName(name: String): Flow<ProductEntity?> = flow {
        Log.d("RemoteDataSource", "Fetching product by name from Firebase")
        try {
            val product = firebaseService.getProductByName(name)
            emit(product)
        } catch (e: Exception) {
            emit(null)
        }
    }

    suspend fun getConsumptionByDates(
        userId: String,
        dates: List<LocalDate>
    ): List<Consumption> {
        val snapshot = firebaseService.getConsumptionByDates(userId, dates)
        Log.e("RemoteDataSource", "snapshot = $snapshot")
        return snapshot.map { it.toConsumption() }
    }


}

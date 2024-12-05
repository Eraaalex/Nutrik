package com.hse.coursework.nutrik.repository

import com.hse.coursework.nutrik.model.Consumption
import com.hse.coursework.nutrik.model.Product
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import java.time.LocalDate
import javax.inject.Inject

interface ConsumptionRepository {
    fun getDailyConsumption(userId: String, date: LocalDate): Flow<List<Consumption>>
    suspend fun updateConsumption(product: Product, newWeight: Double, userId: String)
}

class ConsumptionRepositoryImpl @Inject constructor(
    private val localDataSource: LocalDataSource,
    private val remoteDataSource: RemoteDataSource
) : ConsumptionRepository {

    override suspend fun updateConsumption(product: Product, newWeight: Double, userId: String){
       val consumption = Consumption(
           productId = product.id,
           date = LocalDate.now(),
           weight = newWeight,
           userId = userId
       )

       if (consumption.date.isAfter(LocalDate.now().minusDays(3))) {
           localDataSource.insertConsumption(consumption)
       }
       remoteDataSource.insertConsumption(consumption)
   }
    override fun getDailyConsumption(userId: String, date: LocalDate): Flow<List<Consumption>> {
        // TODO : Implement this method
        return flow {}
    }
}
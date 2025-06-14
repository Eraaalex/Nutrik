package com.hse.coursework.nutrik.repository.consumption

import com.hse.coursework.nutrik.model.Consumption
import com.hse.coursework.nutrik.model.ProductEntity
import com.hse.coursework.nutrik.repository.LocalDataSource
import com.hse.coursework.nutrik.repository.RemoteDataSource
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.launch
import java.time.LocalDate
import javax.inject.Inject

interface ConsumptionRepository {
    fun getDailyConsumption(userId: String, date: LocalDate): Flow<List<Consumption>>
    suspend fun getConsumptionForWeek(
        userId: String,
        startDate: LocalDate,
        endDate: LocalDate,
        cacheScope: CoroutineScope
    ): List<Consumption>

    suspend fun updateConsumption(product: ProductEntity, newWeight: Double, userId: String, date: LocalDate = LocalDate.now())
    suspend fun updateConsumption(product: Consumption, newWeight: Double)
    suspend fun deleteConsumption(entry: Consumption)
}

class ConsumptionRepositoryImpl @Inject constructor(
    private val localDataSource: LocalDataSource,
    private val remoteDataSource: RemoteDataSource
) : ConsumptionRepository {

    override suspend fun updateConsumption(
        product: ProductEntity,
        newWeight: Double,
        userId: String,
        date: LocalDate
    ) {
        val consumption = Consumption(
            productId = product.id,
            date = date,
            weight = newWeight,
            userId = userId,
            productName = product.name
        )

        if (consumption.date.isAfter(LocalDate.now().minusDays(3))) {
            localDataSource.smartInsertConsumption(consumption)
        }
        remoteDataSource.insertConsumption(consumption)
    }

    override suspend fun updateConsumption(
        consumption: Consumption,
        newWeight: Double
    ) {
        val newConsumption = consumption.copy(
            weight = newWeight,
        )

        localDataSource.insertConsumption(newConsumption)
        remoteDataSource.insertConsumption(newConsumption)
    }

    override suspend fun deleteConsumption(entry: Consumption) {

        localDataSource.deleteConsumption(entry)

        remoteDataSource.deleteConsumption(entry)
    }

    override fun getDailyConsumption(userId: String, date: LocalDate): Flow<List<Consumption>> {
        return flow {}
    }

    fun getDatesBetween(startDate: LocalDate, endDate: LocalDate): List<LocalDate> {
        val dates = mutableListOf<LocalDate>()
        var current = startDate
        while (!current.isAfter(endDate)) {
            dates.add(current)
            current = current.plusDays(1)
        }
        return dates
    }

    override suspend fun getConsumptionForWeek(
        userId: String,
        startDate: LocalDate,
        endDate: LocalDate,
        cacheScope: CoroutineScope
    ): List<Consumption> {
        val local = localDataSource.getConsumptionByDateRange(userId, startDate, endDate)
        val localDates = local.map { it.date }.toSet()

        val missingDates = getDatesBetween(startDate, endDate).filterNot { it in localDates }
        if (local.size == missingDates.size) {
            return local
        }
        val remote = if (missingDates.isNotEmpty()) {
            remoteDataSource.getConsumptionByDates(userId, missingDates)
        } else emptyList()

        val all = local + remote

        cacheScope.launch(Dispatchers.IO) {
            if (local.isEmpty()) {
                remote.forEach { localDataSource.insertConsumption(it) }
            }
        }

        return all
    }


}
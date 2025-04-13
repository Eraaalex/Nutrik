package com.hse.coursework.nutrik.data.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.hse.coursework.nutrik.model.ConsumptionEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface ConsumeDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertConsumption(entity: ConsumptionEntity)

    @Delete
    suspend fun removeConsumption(entity: ConsumptionEntity)

    @Query("SELECT productId FROM favorites")
    fun getAllConsumption(): Flow<List<String>>

    @Query("SELECT COUNT(*) FROM favorites WHERE productId = :productId LIMIT 1")
    fun isFavorite(productId: String): Flow<Int>

    @Query(
        """
        SELECT * FROM consumptions 
        WHERE userId = :userId 
        AND date >= :startDate
    """
    )
    fun getRecentConsumptions(userId: String, startDate: String): Flow<List<ConsumptionEntity>>

    @Query(
        """
        SELECT * FROM consumptions 
        WHERE userId = :userId 
        AND date = :date
    """
    )
    fun getDailyConsumptions(userId: String, date: String): Flow<List<ConsumptionEntity>>

    @Query(
        """
        DELETE FROM consumptions 
        WHERE userId = :userId 
        AND date < :cutoffDate
    """
    )
    suspend fun cleanupOldConsumptions(userId: String, cutoffDate: String)

    @Query(
        """
        UPDATE consumptions 
        SET weight = :newWeight 
        WHERE id = :consumptionId 
        AND userId = :userId
    """
    )
    suspend fun updateWeight(userId: String, consumptionId: Long, newWeight: Double)

    @Query(
        """
        SELECT * FROM consumptions 
        WHERE userId = :userId 
        AND date BETWEEN :startDate AND :endDate
    """
    )
    fun getConsumptionsInRange(
        userId: String,
        startDate: String,
        endDate: String
    ): Flow<List<ConsumptionEntity>>

    @Query("SELECT * FROM consumptions WHERE userId = :userId AND date = :date")
    suspend fun getConsumptionByDate(userId: String, date: String): List<ConsumptionEntity>

    @Query(
        """
    SELECT * FROM consumptions
    WHERE userId = :userId AND date BETWEEN :start AND :end
"""
    )
    suspend fun getConsumptionByDateRange(
        userId: String,
        start: String,
        end: String
    ): List<ConsumptionEntity>

}

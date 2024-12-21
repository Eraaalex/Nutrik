package com.hse.coursework.nutrik.model.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.hse.coursework.nutrik.model.FavoriteEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface FavoriteDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertFavorite(entity: FavoriteEntity)

    @Delete
    suspend fun removeFavorite(entity: FavoriteEntity)

    @Query("SELECT productId FROM favorites")
    fun getAllFavoriteIds(): Flow<List<String>>

    @Query("SELECT COUNT(*) FROM favorites WHERE productId = :productId LIMIT 1")
    fun isFavorite(productId: String): Flow<Int>
}

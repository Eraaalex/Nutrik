package com.hse.coursework.nutrik.data.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.hse.coursework.nutrik.model.ProgressEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface ProgressDao {


    @Query(
        """
      SELECT * FROM progress_items
      WHERE userId = :userId AND date = :date
      LIMIT 1
    """
    )
    fun getByUserAndDate(userId: String, date: String): Flow<ProgressEntity?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(progress: ProgressEntity)

    @Query("DELETE FROM progress_items")
    suspend fun clearAll()
}
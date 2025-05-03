package com.hse.coursework.nutrik.data.dao

import androidx.room.Dao
import androidx.room.Entity
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.PrimaryKey
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Entity(tableName = "chat_status")
data class ChatStatusEntity(
    @PrimaryKey val userId: String,
    val lastAdviceText: String,
    val lastAdviceTimestamp: Long,
    val lastConsumptionCount: Int
)

@Entity(tableName = "advice_history")
data class AdviceHistoryEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val userId: String,
    val adviceText: String,
    val timestamp: Long
)

@Dao
interface ChatDao {

    @Query("SELECT * FROM chat_status WHERE userId = :userId LIMIT 1")
    fun getChatStatus(userId: String): Flow<ChatStatusEntity?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertChatStatus(status: ChatStatusEntity)

    @Query("SELECT * FROM advice_history WHERE userId = :userId ORDER BY timestamp DESC")
    fun getAdviceHistory(userId: String): Flow<List<AdviceHistoryEntity>>

    @Insert
    suspend fun insertAdviceHistory(advice: AdviceHistoryEntity)
}

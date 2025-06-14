package com.hse.coursework.nutrik.data

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.hse.coursework.nutrik.data.dao.AdviceHistoryEntity
import com.hse.coursework.nutrik.data.dao.ChatDao
import com.hse.coursework.nutrik.data.dao.ChatStatusEntity
import com.hse.coursework.nutrik.data.dao.ConsumeDao
import com.hse.coursework.nutrik.data.dao.FavoriteDao
import com.hse.coursework.nutrik.data.dao.ProductDao
import com.hse.coursework.nutrik.data.dao.ProgressDao
import com.hse.coursework.nutrik.model.ConsumptionEntity
import com.hse.coursework.nutrik.model.FavoriteEntity
import com.hse.coursework.nutrik.model.ProductEntity
import com.hse.coursework.nutrik.model.ProgressEntity
import com.hse.coursework.nutrik.utils.ConverterUtil

@Database(
    entities = [ProgressEntity::class, ProductEntity::class, FavoriteEntity::class,
        ConsumptionEntity::class, ChatStatusEntity::class, AdviceHistoryEntity::class],
    version = 13,
    exportSchema = true
)
@TypeConverters(ConverterUtil::class)
abstract class AppDatabase : RoomDatabase() {
    abstract fun progressDao(): ProgressDao
    abstract fun productDao(): ProductDao
    abstract fun favoriteDao(): FavoriteDao
    abstract fun consumptionDao(): ConsumeDao

    abstract fun chatDao(): ChatDao
}

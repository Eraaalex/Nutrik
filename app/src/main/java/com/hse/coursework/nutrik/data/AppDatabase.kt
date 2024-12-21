package com.hse.coursework.nutrik.data

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.hse.coursework.nutrik.model.ConsumptionEntity
import com.hse.coursework.nutrik.model.FavoriteEntity
import com.hse.coursework.nutrik.model.Product
import com.hse.coursework.nutrik.model.ProgressItem
import com.hse.coursework.nutrik.model.dao.ConsumeDao
import com.hse.coursework.nutrik.model.dao.FavoriteDao
import com.hse.coursework.nutrik.model.dao.ProductDao
import com.hse.coursework.nutrik.model.dao.ProgressDao
import com.hse.coursework.nutrik.utils.ConverterUtil

@Database(
    entities = [ProgressItem::class, Product::class, FavoriteEntity::class, ConsumptionEntity::class],
    version = 4,
    exportSchema = true
)
@TypeConverters(ConverterUtil::class)
abstract class AppDatabase : RoomDatabase() {
    abstract fun progressDao(): ProgressDao
    abstract fun productDao(): ProductDao
    abstract fun favoriteDao(): FavoriteDao
    abstract fun consumptionDao(): ConsumeDao
}

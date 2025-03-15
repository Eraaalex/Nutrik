package com.hse.coursework.nutrik.di

import android.content.Context
import androidx.room.Room
import com.hse.coursework.nutrik.data.AppDatabase
import com.hse.coursework.nutrik.data.dao.ConsumeDao
import com.hse.coursework.nutrik.data.dao.FavoriteDao
import com.hse.coursework.nutrik.data.dao.ProductDao
import com.hse.coursework.nutrik.data.dao.ProgressDao
import com.hse.coursework.nutrik.repository.progress.ProgressRemoteDataSource
import com.hse.coursework.nutrik.repository.progress.ProgressRepository
import com.hse.coursework.nutrik.repository.progress.ProgressRepositoryImpl
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DataModule {
    @Provides
    @Singleton
    fun provideRoomDatabase(@ApplicationContext context: Context): AppDatabase {

        return Room.databaseBuilder(
            context,
            AppDatabase::class.java,
            "nutrik_database"
        ).fallbackToDestructiveMigration().build()
    }

    @Provides
    @Singleton
    fun provideProgressDao(database: AppDatabase): ProgressDao {
        return database.progressDao()
    }

    @Provides
    @Singleton
    fun provideProductDao(database: AppDatabase): ProductDao {
        return database.productDao()
    }


    @Provides
    @Singleton
    fun provideFavoriteDao(database: AppDatabase): FavoriteDao {
        return database.favoriteDao()
    }


    @Provides
    @Singleton
    fun provideConsumptionDao(database: AppDatabase): ConsumeDao {
        return database.consumptionDao()
    }


    @Provides
    @Singleton
    fun provideProgressRepository(
        dao: ProgressDao,
        remoteDataSource: ProgressRemoteDataSource
    ): ProgressRepository {
        return ProgressRepositoryImpl(dao, remoteDataSource)
    }

}

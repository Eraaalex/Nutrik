package com.hse.coursework.nutrik.di

import android.content.Context
import androidx.room.Room
import com.google.firebase.firestore.FirebaseFirestore
import com.hse.coursework.nutrik.data.AppDatabase
import com.hse.coursework.nutrik.model.dao.ConsumeDao
import com.hse.coursework.nutrik.model.dao.FavoriteDao
import com.hse.coursework.nutrik.model.dao.ProductDao
import com.hse.coursework.nutrik.model.dao.ProgressDao
import com.hse.coursework.nutrik.repository.ConsumptionRepository
import com.hse.coursework.nutrik.repository.ConsumptionRepositoryImpl
import com.hse.coursework.nutrik.repository.FirebaseService
import com.hse.coursework.nutrik.repository.LocalDataSource
import com.hse.coursework.nutrik.repository.ProgressRepository
import com.hse.coursework.nutrik.repository.RemoteDataSource
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
    fun provideFirebaseFirestore(): FirebaseFirestore {
        return FirebaseFirestore.getInstance()
    }


    @Provides
    @Singleton
    fun provideLocalDataSource(
        progressDao: ProgressDao,
        productDao: ProductDao,
        favoriteDao: FavoriteDao,
        consumptionDao: ConsumeDao
    ): LocalDataSource {
        return LocalDataSource(progressDao, productDao, favoriteDao, consumptionDao)
    }


    @Provides
    @Singleton
    fun provideRemoteDataSource(firebaseService: FirebaseService): RemoteDataSource {
        return RemoteDataSource(firebaseService)
    }

    @Provides
    @Singleton
    fun provideFirebaseService(firebaseFirestore: FirebaseFirestore): FirebaseService {
        return FirebaseService(firebaseFirestore)
    }

    @Provides
    @Singleton
    fun provideProgressRepository(
        localDataSource: LocalDataSource,
        remoteDataSource: RemoteDataSource
    ): ProgressRepository {
        return ProgressRepository(localDataSource, remoteDataSource)
    }


    @Provides
    @Singleton
    fun provideConsumptionRepository(
        localDataSource: LocalDataSource,
        remoteDataSource: RemoteDataSource
    ): ConsumptionRepository {
        return ConsumptionRepositoryImpl(localDataSource, remoteDataSource)
    }
}

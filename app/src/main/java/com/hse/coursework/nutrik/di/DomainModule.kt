package com.hse.coursework.nutrik.di

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.hse.coursework.nutrik.data.dao.ConsumeDao
import com.hse.coursework.nutrik.data.dao.FavoriteDao
import com.hse.coursework.nutrik.data.dao.ProductDao
import com.hse.coursework.nutrik.data.dao.ProgressDao
import com.hse.coursework.nutrik.repository.LocalDataSource
import com.hse.coursework.nutrik.repository.RemoteDataSource
import com.hse.coursework.nutrik.repository.chat.ChatRepository
import com.hse.coursework.nutrik.repository.consumption.ConsumptionRepository
import com.hse.coursework.nutrik.repository.consumption.ConsumptionRepositoryImpl
import com.hse.coursework.nutrik.repository.product.ProductRepository
import com.hse.coursework.nutrik.repository.progress.ProgressRemoteDataSource
import com.hse.coursework.nutrik.repository.user.UserRepository
import com.hse.coursework.nutrik.service.BarcodeScanner
import com.hse.coursework.nutrik.service.FirebaseService
import com.hse.coursework.nutrik.service.RemoteAuthService
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DomainModule {
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
    fun provideConsumptionRepository(
        localDataSource: LocalDataSource,
        remoteDataSource: RemoteDataSource
    ): ConsumptionRepository {
        return ConsumptionRepositoryImpl(localDataSource, remoteDataSource)
    }

    @Provides
    @Singleton
    fun provideUserRepository(
        remoteDataSource: RemoteAuthService
    ): UserRepository {
        return UserRepository(remoteAuthService = remoteDataSource)
    }

    @Provides
    @Singleton
    fun provideRemoteAuthService(
        firebaseFirestore: FirebaseFirestore,
        firebaseAuth: FirebaseAuth
    ): RemoteAuthService {
        return RemoteAuthService(firebaseFirestore, firebaseAuth)
    }

    @Provides
    @Singleton
    fun provideChatRepository(
    ): ChatRepository {
        return ChatRepository()
    }

    @Provides
    @Singleton
    fun provideBarcodeScanner(productRepository: ProductRepository): BarcodeScanner {
        return BarcodeScanner(productRepository)
    }
}

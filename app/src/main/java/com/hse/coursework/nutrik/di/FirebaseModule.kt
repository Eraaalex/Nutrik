package com.hse.coursework.nutrik.di

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.hse.coursework.nutrik.auth.AuthRepository
import com.hse.coursework.nutrik.service.FirebaseService
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object FirebaseModule {

    @Provides
    @Singleton
    fun provideFirebaseAuth(): FirebaseAuth {
        return FirebaseAuth.getInstance()
    }

    @Provides
    @Singleton
    fun provideAuthRepository(firebaseAuth: FirebaseAuth): AuthRepository {
        return AuthRepository(firebaseAuth)
    }

    @Provides
    @Singleton
    fun provideFirebaseFirestore(): FirebaseFirestore {
        return FirebaseFirestore.getInstance()
    }

    @Provides
    @Singleton
    fun provideFirebaseService(firebaseFirestore: FirebaseFirestore): FirebaseService {
        return FirebaseService(firebaseFirestore)
    }
}

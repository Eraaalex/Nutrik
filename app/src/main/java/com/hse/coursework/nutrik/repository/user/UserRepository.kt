package com.hse.coursework.nutrik.repository.user

import android.util.Log
import com.hse.coursework.nutrik.model.dto.User
import com.hse.coursework.nutrik.service.RemoteAuthService
import javax.inject.Inject


class UserRepository @Inject constructor(
    private val remoteAuthService: RemoteAuthService
) {
    private var cachedUser: User? = null

    suspend fun getUser(forceRefresh: Boolean = false): User? {
        if (cachedUser != null && !forceRefresh) return cachedUser

        val user = remoteAuthService.fetchUserData()
        Log.e("UserRepository", "Fetched user: $user")
        cachedUser = user
        return user
    }

    suspend fun saveUser(user: User) {
        remoteAuthService.saveUserData(user)
        cachedUser = user
    }

    fun clearCache() {
        cachedUser = null
    }
}

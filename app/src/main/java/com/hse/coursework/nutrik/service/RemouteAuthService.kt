package com.hse.coursework.nutrik.service

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.hse.coursework.nutrik.model.FirestoreUserDTO
import com.hse.coursework.nutrik.model.dto.User
import com.hse.coursework.nutrik.model.toDTO
import com.hse.coursework.nutrik.model.toDomain
import kotlinx.coroutines.tasks.await
import javax.inject.Inject

class RemoteAuthService @Inject constructor(
    private val firestore: FirebaseFirestore,
    private val firebaseAuth: FirebaseAuth
) {

    suspend fun fetchUserData(): User? {
        val uid = firebaseAuth.currentUser?.uid ?: return null
        val snapshot =
            firestore.collection("users").document(uid).collection("userData").document("profile")
                .get().await()

        return snapshot.toObject(FirestoreUserDTO::class.java)?.toDomain()
    }

    suspend fun saveUserData(user: User) {
        val email = firebaseAuth.currentUser?.email ?: ""
        val uid = firebaseAuth.currentUser?.uid ?: return
        firestore.collection("users").document(uid).collection("userData").document("profile")
            .set(user.copy(email = email).toDTO()).await()
    }
}

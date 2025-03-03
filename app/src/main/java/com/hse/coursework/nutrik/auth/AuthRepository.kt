package com.hse.coursework.nutrik.auth

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.auth.UserProfileChangeRequest
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.suspendCancellableCoroutine
import javax.inject.Inject
import kotlin.coroutines.resume

class AuthRepository @Inject constructor(
    private val firebaseAuth: FirebaseAuth
) {

    private val _authStateFlow = MutableStateFlow(firebaseAuth.currentUser?.isEmailVerified == true)
    val authStateFlow: StateFlow<Boolean> = _authStateFlow

    suspend fun register(
        email: String,
        password: String,
        nickname: String
    ): Result<Unit> = suspendCancellableCoroutine { cont ->
        firebaseAuth.createUserWithEmailAndPassword(email, password)
            .addOnCompleteListener { createTask ->
                if (createTask.isSuccessful) {
                    val user = firebaseAuth.currentUser
                    user?.updateProfile(
                        UserProfileChangeRequest.Builder()
                            .setDisplayName(nickname)
                            .build()
                    )?.addOnCompleteListener { profileTask ->
                        if (profileTask.isSuccessful) {
                            user.sendEmailVerification()
                                .addOnCompleteListener { verifyTask ->
                                    if (verifyTask.isSuccessful) cont.resume(Result.success(Unit))
                                    else cont.resume(Result.failure(verifyTask.exception!!))
                                }
                        } else {
                            cont.resume(Result.failure(profileTask.exception!!))
                        }
                    }
                } else {
                    cont.resume(Result.failure(createTask.exception!!))
                }
            }
    }


    suspend fun login(
        email: String,
        password: String
    ): Result<Unit> = suspendCancellableCoroutine { cont ->
        firebaseAuth.signInWithEmailAndPassword(email, password)
            .addOnCompleteListener { signInTask ->
                if (signInTask.isSuccessful) {
                    val user = firebaseAuth.currentUser
                    if (user != null && user.isEmailVerified) {
                        cont.resume(Result.success(Unit))
                    } else {
                        user?.sendEmailVerification()
                        cont.resume(Result.failure(Exception("Email не подтвержден. Мы отправили письмо повторно.")))
                    }
                } else {
                    cont.resume(Result.failure(signInTask.exception!!))
                }
            }
    }

    fun isUserAuthenticated(): Boolean {
        return firebaseAuth.currentUser?.isEmailVerified == true
    }

    suspend fun signInWithGoogle(idToken: String): Result<Unit> =
        suspendCancellableCoroutine { cont ->
            val credential = GoogleAuthProvider.getCredential(idToken, null)
            firebaseAuth.signInWithCredential(credential)
                .addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        cont.resume(Result.success(Unit))
                    } else {
                        cont.resume(
                            Result.failure(
                                task.exception ?: Exception("Ошибка авторизации через Google")
                            )
                        )
                    }
                }
        }

}

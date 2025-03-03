package com.hse.coursework.nutrik.ui.theme.screen.auth

import android.app.Application
import android.content.Intent
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.hse.coursework.nutrik.auth.AuthRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class AuthViewModel @Inject constructor(
    application: Application,
    private val repository: AuthRepository
) : AndroidViewModel(application) {
    var uiState by mutableStateOf(AuthUiState())
        private set

    val isUserAuthenticated: StateFlow<Boolean> = repository.authStateFlow
    private val _googleSignInIntent = MutableSharedFlow<Intent>()
    val googleSignInIntent: SharedFlow<Intent> = _googleSignInIntent

    fun onGoogleSignInRequest() {
        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken("YOUR_WEB_CLIENT_ID") // из Firebase settings
            .requestEmail()
            .build()
        val client = GoogleSignIn.getClient(getApplication<Application>(), gso)
        viewModelScope.launch {
            _googleSignInIntent.emit(client.signInIntent)
        }
    }

    fun onGoogleSignInSuccess(idToken: String) {
        viewModelScope.launch {
            uiState = uiState.copy(isLoading = true)
            val result = repository.signInWithGoogle(idToken)
            result.fold(
                onSuccess = { uiState = uiState.copy(isLoading = false, isSuccess = true) },
                onFailure = { uiState = uiState.copy(isLoading = false, errorMessage = it.message) }
            )
        }
    }


    fun onEvent(event: AuthEvent) {
        when (event) {
            is AuthEvent.EmailChanged -> uiState =
                uiState.copy(email = event.value, errorMessage = null)

            is AuthEvent.PasswordChanged -> uiState =
                uiState.copy(password = event.value, errorMessage = null)

            is AuthEvent.ConfirmPasswordChanged -> uiState =
                uiState.copy(confirmPassword = event.value, errorMessage = null)

            is AuthEvent.NicknameChanged -> uiState =
                uiState.copy(nickname = event.value, errorMessage = null)

            AuthEvent.ToggleMode -> uiState = uiState.copy(
                isRegister = !uiState.isRegister,
                email = "",
                password = "",
                confirmPassword = "",
                nickname = "",
                errorMessage = null
            )

            AuthEvent.Submit -> submit()
        }
    }

    private fun submit() {
        val state = uiState
        viewModelScope.launch {
            uiState = state.copy(isLoading = true, errorMessage = null)
            val result = if (state.isRegister) {
                if (state.password != state.confirmPassword) {
                    Result.failure<Unit>(Exception("Пароли не совпадают"))
                } else {
                    repository.register(state.email, state.password, state.nickname)
                }
            } else {
                repository.login(state.email, state.password)
            }

            result.fold(
                onSuccess = {
                    uiState = uiState.copy(isLoading = false, isSuccess = true)
                },
                onFailure = { err ->
                    val message = when {
                        err.message?.contains("badly formatted", ignoreCase = true) == true ||
                                err.message?.contains(
                                    "WEAK_PASSWORD",
                                    ignoreCase = true
                                ) == true -> {
                            "Некорректный ввод данных пользователя"
                        }

                        err.message?.contains("no user record", ignoreCase = true) == true ||
                                err.message?.contains(
                                    "password is invalid",
                                    ignoreCase = true
                                ) == true ||
                                err.message?.contains(
                                    "credential is incorrect",
                                    ignoreCase = true
                                ) == true -> {
                            "Некорректный ввод данных, такого пользователя не существует"
                        }

                        else -> err.message ?: "Неизвестная ошибка"
                    }

                    uiState = uiState.copy(isLoading = false, errorMessage = message)
                }
            )
        }
    }
}


sealed class AuthEvent {
    data class EmailChanged(val value: String) : AuthEvent()
    data class PasswordChanged(val value: String) : AuthEvent()
    data class ConfirmPasswordChanged(val value: String) : AuthEvent()
    data class NicknameChanged(val value: String) : AuthEvent()
    object ToggleMode : AuthEvent()
    object Submit : AuthEvent()
}

data class AuthUiState(
    val isRegister: Boolean = false,
    val email: String = "",
    val password: String = "",
    val confirmPassword: String = "",
    val nickname: String = "",
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
    val isSuccess: Boolean = false
)


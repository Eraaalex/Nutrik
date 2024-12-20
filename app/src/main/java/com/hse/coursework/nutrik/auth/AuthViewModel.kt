package com.hse.coursework.nutrik.auth

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.ActionCodeSettings
import com.google.firebase.auth.FirebaseAuth
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.stateIn
import javax.inject.Inject

@HiltViewModel
class AuthViewModel @Inject constructor(
    private val authRepository: AuthRepository
) : ViewModel() {

    // Состояние пользователя (авторизован или нет)
    val isUserAuthenticated: StateFlow<Boolean> = flow {
        emit(authRepository.isUserAuthenticated())
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), false)

    // LiveData для сообщений и ошибок
    private val _message = MutableStateFlow<String?>(null)
    val message: StateFlow<String?> = _message

    private val _isCodeSent = MutableStateFlow(false)
    val isCodeSent: StateFlow<Boolean> = _isCodeSent

    fun sendVerificationCode(email: String, onSuccess: () -> Unit, onFailure: (Exception) -> Unit) {
        if (email.isBlank() || !isValidEmail(email)) {
            _message.value = "Введите корректный email"
            return
        }

        authRepository.sendVerificationCode(
            email = email,
            onSuccess = onSuccess,
            onFailure = onFailure
        )
    }

    fun verifyCodeAndLogin(email: String, code: String, onSuccess: () -> Unit, onFailure: (Exception) -> Unit) {
        if (email.isBlank() || !isValidEmail(email)) {
            _message.value = "Введите корректный email"
            return
        }

        if (code.isBlank() || code.length != 4) {
            _message.value = "Введите корректный код"
            return
        }

        authRepository.verifyCode(
            email = email,
            inputCode = code,
            onSuccess = {
                authRepository.registerOrLogin(
                    email = email,
                    onSuccess = {
                        _message.value = "Успешный вход"
                        onSuccess()
                    },
                    onFailure = { error ->
                        _message.value = error.localizedMessage ?: "Ошибка при входе"
                    }
                )
            },
            onFailure = { error ->
                _message.value = error.localizedMessage ?: "Неверный код подтверждения"
            }
        )
    }

    private fun isValidEmail(email: String): Boolean {
        return android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()
    }

    // Очистка сообщений после их отображения
    fun clearMessage() {
        _message.value = null
    }
}



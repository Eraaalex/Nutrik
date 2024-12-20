package com.hse.coursework.nutrik.screen

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.Button
import androidx.compose.material.Text
import androidx.compose.material.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.hse.coursework.nutrik.auth.AuthViewModel

@Composable
fun AuthScreen(authViewModel: AuthViewModel = hiltViewModel(), onAuthSuccess: () -> Unit) {
    var email by remember { mutableStateOf("") }
    var code by remember { mutableStateOf("") }
    var message by remember { mutableStateOf("") }
    var isCodeSent by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        TextField(
            value = email,
            onValueChange = { email = it },
            label = { Text("Введите email") },
            modifier = Modifier.fillMaxWidth()
        )
        if (isCodeSent) {
            Spacer(modifier = Modifier.height(8.dp))
            TextField(
                value = code,
                onValueChange = { code = it },
                label = { Text("Введите код") },
                modifier = Modifier.fillMaxWidth()
            )
        }
        Spacer(modifier = Modifier.height(8.dp))
        Button(onClick = {
            if (!isCodeSent) {
                authViewModel.sendVerificationCode(
                    email = email,
                    onSuccess = {
                        message = "Код отправлен!"
                        isCodeSent = true
                    },
                    onFailure = { error ->
                        message = error.localizedMessage ?: "Ошибка"
                    }
                )
            } else {
                authViewModel.verifyCodeAndLogin(
                    email = email,
                    code = code,
                    onSuccess = onAuthSuccess,
                    onFailure = { error ->
                        message = error.localizedMessage ?: "Ошибка"
                    }
                )
            }
        }) {
            Text(if (!isCodeSent) "Отправить код" else "Подтвердить код")
        }
        Spacer(modifier = Modifier.height(16.dp))
        Text(message)
    }
}

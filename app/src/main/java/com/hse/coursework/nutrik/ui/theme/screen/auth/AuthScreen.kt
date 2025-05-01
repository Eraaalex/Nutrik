package com.hse.coursework.nutrik.ui.theme.screen.auth

import android.util.Log
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Button
import androidx.compose.material.ButtonDefaults
import androidx.compose.material.Icon
import androidx.compose.material.MaterialTheme
import androidx.compose.material.OutlinedTextField
import androidx.compose.material.Text
import androidx.compose.material.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.common.api.ApiException
import com.hse.coursework.nutrik.R

@Composable
fun AuthScreen(
    viewModel: AuthViewModel = hiltViewModel(),
    onAuthSuccess: () -> Unit
) {
    val state by remember { mutableStateOf(viewModel.uiState) }
    LaunchedEffect(viewModel.uiState.isSuccess) {
        if (viewModel.uiState.isSuccess) onAuthSuccess()
    }

    val showResetDialog = remember { mutableStateOf(false) }
    val resetEmail = remember { mutableStateOf("") }


    val context = LocalContext.current
    val launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) { result ->
        val task = GoogleSignIn.getSignedInAccountFromIntent(result.data)
        try {
            val account = task.getResult(ApiException::class.java)
            viewModel.onGoogleSignInSuccess(account.idToken!!)
        } catch (e: ApiException) {

            Log.e("Auth", "Google sign-in failed", e)
        }
    }
    LaunchedEffect(Unit) {
        viewModel.googleSignInIntent.collect { intent ->
            launcher.launch(intent)
        }
    }


    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFfffcdf))
            .padding(16.dp),
        verticalArrangement = Arrangement.Center
    ) {

        Text(
            text = if (viewModel.uiState.isRegister) "Регистрация" else "Авторизация",
            style = MaterialTheme.typography.h5,
            modifier = Modifier.padding(bottom = 24.dp),
            color = Color(0xFF4E2215),
        )

        OutlinedTextField(
            value = viewModel.uiState.email,
            onValueChange = { viewModel.onEvent(AuthEvent.EmailChanged(it)) },
            label = { Text("Email") },
            modifier = Modifier.fillMaxWidth()
        )

        OutlinedTextField(
            value = viewModel.uiState.password,
            onValueChange = { viewModel.onEvent(AuthEvent.PasswordChanged(it)) },
            label = { Text("Пароль") },
            visualTransformation = PasswordVisualTransformation(),
            modifier = Modifier.fillMaxWidth()
        )

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 4.dp, bottom = 8.dp),
            horizontalArrangement = Arrangement.End
        ) {
            if (!viewModel.uiState.isRegister) {
                TextButton(onClick = {
                     showResetDialog.value = true

                }) {
                    Text("Забыли пароль?", color = Color(0xFF4E2215))
                }
            }
        }


        if (viewModel.uiState.isRegister) {
            OutlinedTextField(
                value = viewModel.uiState.confirmPassword,
                onValueChange = { viewModel.onEvent(AuthEvent.ConfirmPasswordChanged(it)) },
                label = { Text("Повторите пароль") },
                visualTransformation = PasswordVisualTransformation(),
                modifier = Modifier.fillMaxWidth()
            )
            OutlinedTextField(
                value = viewModel.uiState.nickname,
                onValueChange = { viewModel.onEvent(AuthEvent.NicknameChanged(it)) },
                label = { Text("Никнейм") },
                modifier = Modifier.fillMaxWidth()
            )
        }

        viewModel.uiState.errorMessage?.let {
            Text(text = it, color = Color.Red, modifier = Modifier.padding(top = 8.dp))
        }

        Button(
            onClick = { viewModel.onEvent(AuthEvent.Submit) },
            enabled = !viewModel.uiState.isLoading,
            modifier = Modifier
                .fillMaxWidth()
                .height(52.dp)
                .padding(top = 16.dp),
            colors = ButtonDefaults.buttonColors(
                backgroundColor = Color(0xFFFDF6C8)
            ),
            shape = RoundedCornerShape(12.dp)
        ) {
            Text(
                text = if (viewModel.uiState.isRegister) "Зарегистрироваться" else "Войти",
                color = Color(0xFF4E2215),
                textAlign = TextAlign.Center
            )
        }

        Spacer(modifier = Modifier.height(8.dp))

        Button(
            onClick = { viewModel.onGoogleSignInRequest() },
            modifier = Modifier
                .fillMaxWidth()
                .height(32.dp),
            colors = ButtonDefaults.buttonColors(backgroundColor = Color(0xFFFDF6C8)),
            shape = RoundedCornerShape(12.dp)
        ) {
            Icon(
                painter = painterResource(id = R.drawable.google),
                contentDescription = "Google",
                tint = Color.Unspecified
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text("Войти через Google", color = Color.Black)
        }
        Spacer(modifier = Modifier.height(8.dp))

        TextButton(
            onClick = { viewModel.onEvent(AuthEvent.ToggleMode) },
            modifier = Modifier.align(Alignment.CenterHorizontally)
        ) {
            Text(
                if (viewModel.uiState.isRegister)
                    "Уже есть аккаунт? Войти" else "Нет аккаунта? Зарегистрироваться",
                color = Color(0xFF4E2215)
            )
        }


    }

    if (showResetDialog.value) {
        androidx.compose.material.AlertDialog(
            onDismissRequest = { showResetDialog.value = false },
            title = { Text("Сброс пароля",  color = Color(0xFF4E2215)) },
            backgroundColor = Color(0xFFfffcdf),
            text = {
                Column {
                    Text("Введите ваш email для получения инструкции по сбросу пароля.",  color = Color(0xFF4E2215))
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = resetEmail.value,
                        onValueChange = { resetEmail.value = it },
                        label = { Text("Email") },
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                TextButton(onClick = {
                    // viewModel.onResetPasswordRequest(resetEmail.value)
                    showResetDialog.value = false
                }) {
                    Text("Отправить",  color = Color(0xFF4E2215))
                }
            },
            dismissButton = {
                TextButton(onClick = { showResetDialog.value = false }) {
                    Text("Отмена",  color = Color(0xFF4E2215))
                }
            }
        )
    }


}

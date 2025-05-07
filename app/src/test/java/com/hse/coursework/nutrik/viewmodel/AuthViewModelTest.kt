package com.hse.coursework.nutrik.viewmodel

import android.app.Application
import com.hse.coursework.nutrik.auth.AuthRepository
import com.hse.coursework.nutrik.ui.theme.screen.auth.*
import io.mockk.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.test.*
import org.junit.*
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue

@OptIn(ExperimentalCoroutinesApi::class)
class AuthViewModelTest {

    private val repository: AuthRepository = mockk()
    private val application: Application = mockk(relaxed = true)

    private lateinit var viewModel: AuthViewModel

    private val dispatcher = StandardTestDispatcher()

    @Before
    fun setup() {
        Dispatchers.setMain(StandardTestDispatcher())
        every { repository.authStateFlow } returns MutableStateFlow(false)
        viewModel = AuthViewModel(application, repository)
    }


    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `email change updates uiState`() {
        viewModel.onEvent(AuthEvent.EmailChanged("user@example.com"))
        assertEquals("user@example.com", viewModel.uiState.email)
    }

    @Test
    fun `password change updates uiState`() {
        viewModel.onEvent(AuthEvent.PasswordChanged("123456"))
        assertEquals("123456", viewModel.uiState.password)
    }

    @Test
    fun `confirm password change updates uiState`() {
        viewModel.onEvent(AuthEvent.ConfirmPasswordChanged("123456"))
        assertEquals("123456", viewModel.uiState.confirmPassword)
    }

    @Test
    fun `nickname change updates uiState`() {
        viewModel.onEvent(AuthEvent.NicknameChanged("TestUser"))
        assertEquals("TestUser", viewModel.uiState.nickname)
    }

    @Test
    fun `toggle mode switches mode and clears fields`() {
        viewModel.onEvent(AuthEvent.EmailChanged("user@example.com"))
        viewModel.onEvent(AuthEvent.PasswordChanged("123"))
        viewModel.onEvent(AuthEvent.ToggleMode)

        assertTrue(viewModel.uiState.isRegister)
        assertEquals("", viewModel.uiState.email)
        assertEquals("", viewModel.uiState.password)
    }

    @Test
    fun `submit with mismatched passwords shows error`() = runTest {
        viewModel.onEvent(AuthEvent.ToggleMode) // enter register mode
        viewModel.onEvent(AuthEvent.EmailChanged("user@example.com"))
        viewModel.onEvent(AuthEvent.PasswordChanged("123"))
        viewModel.onEvent(AuthEvent.ConfirmPasswordChanged("456"))
        viewModel.onEvent(AuthEvent.NicknameChanged("TestUser"))

        viewModel.onEvent(AuthEvent.Submit)
        advanceUntilIdle()

        assertEquals("Пароли не совпадают", viewModel.uiState.errorMessage)
        assertFalse(viewModel.uiState.isSuccess)
    }

    @Test
    fun `submit register success updates isSuccess`() = runTest {
        coEvery { repository.register(any(), any(), any()) } returns Result.success(Unit)

        viewModel.onEvent(AuthEvent.ToggleMode)
        viewModel.onEvent(AuthEvent.EmailChanged("user@example.com"))
        viewModel.onEvent(AuthEvent.PasswordChanged("123456"))
        viewModel.onEvent(AuthEvent.ConfirmPasswordChanged("123456"))
        viewModel.onEvent(AuthEvent.NicknameChanged("TestUser"))
        viewModel.onEvent(AuthEvent.Submit)

        advanceUntilIdle()

        assertTrue(viewModel.uiState.isSuccess)
        assertFalse(viewModel.uiState.isLoading)
    }

    @Test
    fun `submit login success updates isSuccess`() = runTest {
        coEvery { repository.login(any(), any()) } returns Result.success(Unit)

        viewModel.onEvent(AuthEvent.EmailChanged("user@example.com"))
        viewModel.onEvent(AuthEvent.PasswordChanged("123456"))
        viewModel.onEvent(AuthEvent.Submit)

        advanceUntilIdle()

        assertTrue(viewModel.uiState.isSuccess)
    }

    @Test
    fun `submit login failure with badly formatted email returns friendly message`() = runTest {
        coEvery {
            repository.login(any(), any())
        } returns Result.failure(Exception("The email address is badly formatted"))

        viewModel.onEvent(AuthEvent.EmailChanged("bad-email"))
        viewModel.onEvent(AuthEvent.PasswordChanged("123456"))
        viewModel.onEvent(AuthEvent.Submit)

        advanceUntilIdle()

        assertEquals("Некорректный ввод данных пользователя", viewModel.uiState.errorMessage)
    }

    @Test
    fun `submit login failure with unknown error returns fallback message`() = runTest {
        coEvery {
            repository.login(any(), any())
        } returns Result.failure(Exception("Unknown Firebase failure"))

        viewModel.onEvent(AuthEvent.EmailChanged("user@example.com"))
        viewModel.onEvent(AuthEvent.PasswordChanged("123456"))
        viewModel.onEvent(AuthEvent.Submit)

        advanceUntilIdle()

        assertEquals("Unknown Firebase failure", viewModel.uiState.errorMessage)
    }

    @Test
    fun `onGoogleSignInSuccess triggers repository and sets isSuccess`() = runTest {
        coEvery { repository.signInWithGoogle("token123") } returns Result.success(Unit)

        viewModel.onGoogleSignInSuccess("token123")
        advanceUntilIdle()

        assertTrue(viewModel.uiState.isSuccess)
    }
}

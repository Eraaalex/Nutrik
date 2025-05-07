package com.hse.coursework.nutrik.repository


import com.google.android.gms.tasks.OnCompleteListener
import com.google.android.gms.tasks.Task
import com.google.firebase.auth.AuthCredential
import com.google.firebase.auth.AuthResult
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.UserProfileChangeRequest
import com.hse.coursework.nutrik.auth.AuthRepository
import io.mockk.every
import io.mockk.mockk
import io.mockk.mockkConstructor
import io.mockk.verify
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class AuthRepositoryTest {

    private val firebaseAuth: FirebaseAuth = mockk(relaxed = true)
    private val firebaseUser: FirebaseUser = mockk(relaxed = true)
    private val authResult: AuthResult = mockk()
    private val credential: AuthCredential = mockk()
    private lateinit var repository: AuthRepository

    @Before
    fun setup() {
        every { firebaseAuth.currentUser } returns firebaseUser
        every { firebaseUser.isEmailVerified } returns true
        repository = AuthRepository(firebaseAuth)
    }

    @Test
    fun `authStateFlow reflects currentUser isEmailVerified true`() {
        assertTrue(repository.authStateFlow.value)
    }

    @Test
    fun `isUserAuthenticated returns true when email is verified`() {
        every { firebaseUser.isEmailVerified } returns true
        assertTrue(repository.isUserAuthenticated())
    }

    @Test
    fun `register success completes with success`() = runTest {
        val mockRequest = mockk<UserProfileChangeRequest>(relaxed = true)
        val mockBuilder = mockk<UserProfileChangeRequest.Builder>()

        val createTask: Task<AuthResult> = mockTask(success = true)
        val updateTask: Task<Void> = mockTask(success = true)
        val verifyTask: Task<Void> = mockTask(success = true)

        every { firebaseAuth.createUserWithEmailAndPassword(any(), any()) } returns createTask
        every { firebaseUser.updateProfile(mockRequest) } returns updateTask
        every { firebaseUser.sendEmailVerification() } returns verifyTask
        every { firebaseAuth.currentUser } returns firebaseUser

        mockkConstructor(UserProfileChangeRequest.Builder::class)
        every { anyConstructed<UserProfileChangeRequest.Builder>().setDisplayName("Nickname") } returns mockBuilder
        every { mockBuilder.build() } returns mockRequest

        val result = repository.register("test@example.com", "123456", "Nickname")

        assertTrue(result.isSuccess)
        verify { firebaseUser.updateProfile(mockRequest) }
    }



    @Test
    fun `register fails when createUser fails`() = runTest {
        val exception = Exception("Creation failed")
        val createTask: Task<AuthResult> = mockTask(success = false, exception = exception)

        every { firebaseAuth.createUserWithEmailAndPassword(any(), any()) } returns createTask

        val result = repository.register("fail@example.com", "123", "nick")
        assertTrue(result.isFailure)
        assertEquals("Creation failed", result.exceptionOrNull()?.message)
    }

    @Test
    fun `login succeeds when email verified`() = runTest {
        val loginTask: Task<AuthResult> = mockTask(success = true)

        every { firebaseAuth.signInWithEmailAndPassword(any(), any()) } returns loginTask
        every { firebaseAuth.currentUser } returns firebaseUser
        every { firebaseUser.isEmailVerified } returns true

        val result = repository.login("user@mail.com", "pass")
        assertTrue(result.isSuccess)
    }

    @Test
    fun `login fails when email not verified`() = runTest {
        val loginTask: Task<AuthResult> = mockTask(success = true)

        every { firebaseAuth.signInWithEmailAndPassword(any(), any()) } returns loginTask
        every { firebaseAuth.currentUser } returns firebaseUser
        every { firebaseUser.isEmailVerified } returns false
        every { firebaseUser.sendEmailVerification() } returns mockTask(true)

        val result = repository.login("user@mail.com", "pass")
        assertTrue(result.isFailure)
        assertTrue(result.exceptionOrNull()?.message?.contains("Email не подтвержден") == true)
    }

    @Test
    fun `signInWithGoogle success returns success`() = runTest {
        val googleTask: Task<AuthResult> = mockTask(success = true)
        every { firebaseAuth.signInWithCredential(any()) } returns googleTask

        val result = repository.signInWithGoogle("token123")
        assertTrue(result.isSuccess)
    }

    @Test
    fun `signInWithGoogle failure returns failure`() = runTest {
        val exception = Exception("Google login failed")
        val googleTask: Task<AuthResult> = mockTask(success = false, exception = exception)
        every { firebaseAuth.signInWithCredential(any()) } returns googleTask

        val result = repository.signInWithGoogle("token123")
        assertTrue(result.isFailure)
        assertEquals("Google login failed", result.exceptionOrNull()?.message)
    }

    private inline fun <reified T> mockTask(
        success: Boolean,
        exception: Exception? = null
    ): Task<T> {
        val task = mockk<Task<T>>(relaxed = true)
        every { task.isSuccessful } returns success
        every { task.exception } returns exception
        every { task.addOnCompleteListener(any()) } answers {
            val listener = it.invocation.args[0] as OnCompleteListener<T>
            listener.onComplete(task)
            task
        }
        return task
    }
}

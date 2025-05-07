@file:OptIn(kotlinx.coroutines.ExperimentalCoroutinesApi::class)

package com.hse.coursework.nutrik.repository

import android.util.Log
import com.hse.coursework.nutrik.model.Restriction
import com.hse.coursework.nutrik.model.dto.Gender
import com.hse.coursework.nutrik.model.dto.User
import com.hse.coursework.nutrik.repository.user.UserRepository
import com.hse.coursework.nutrik.service.RemoteAuthService
import io.mockk.Runs
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.just
import io.mockk.mockk
import io.mockk.mockkStatic
import io.mockk.unmockkAll
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class UserRepositoryTest {

    private val remoteAuthService: RemoteAuthService = mockk()
    private lateinit var repo: UserRepository
    private val testDispatcher = StandardTestDispatcher()

    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)
        mockkStatic(Log::class)
        every { Log.e(any(), any()) } returns 0
        repo = UserRepository(remoteAuthService)
    }

    @After
    fun teardown() {
        unmockkAll()
        Dispatchers.resetMain()
    }

    @Test
    fun `getUser returns cached user if not forceRefresh`() = runTest {
        val user = User("id", listOf(Restriction.NUT), 28, Gender.FEMALE)

        repo.clearCache()

        coEvery { remoteAuthService.saveUserData(user) } just Runs
        repo.saveUser(user)

        val result = repo.getUser()
        assertEquals(user, result)
    }

    @Test
    fun `getUser fetches from remote if cache is empty`() = runTest {
        val user = User("id", listOf(Restriction.NUT), 28, Gender.FEMALE)
        repo.clearCache()
        coEvery { remoteAuthService.fetchUserData() } returns user

        val result = repo.getUser()
        assertEquals(user, result)
        // и кэш теперь тоже обновлён
        val cached = repo.getUser()
        assertEquals(user, cached)
        coVerify(exactly = 1) { remoteAuthService.fetchUserData() }
    }

    @Test
    fun `getUser with forceRefresh fetches from remote even if cache exists`() = runTest {
        val userOld = User("id", listOf(Restriction.NUT), 28, Gender.FEMALE)
        val userNew = User("id", listOf(Restriction.NUT), 29, Gender.FEMALE)
        repo.clearCache()
        coEvery { remoteAuthService.fetchUserData() } returnsMany listOf(userOld, userNew)

        // сначала кэшируем старого
        val first = repo.getUser(forceRefresh = true)
        assertEquals(userOld, first)
        // потом просим новый с forceRefresh
        coEvery { remoteAuthService.fetchUserData() } returns userNew
        val second = repo.getUser(forceRefresh = true)
        assertEquals(userNew, second)
        coVerify(atLeast = 2) { remoteAuthService.fetchUserData() }
    }

    @Test
    fun `saveUser calls remote and updates cache`() = runTest {
        val user = User("id", listOf(Restriction.NUT), 28, Gender.FEMALE)
        coEvery { remoteAuthService.saveUserData(user) } just Runs
        repo.saveUser(user)

        val cached = repo.getUser()
        assertEquals(user, cached)
        coVerify { remoteAuthService.saveUserData(user) }
    }

    @Test
    fun `clearCache actually clears the user`() = runTest {
        val user = User("id", listOf(Restriction.NUT), 28, Gender.FEMALE)
        coEvery { remoteAuthService.saveUserData(user) } just Runs
        repo.saveUser(user)
        assertEquals(user, repo.getUser())
        repo.clearCache()

        coEvery { remoteAuthService.fetchUserData() } returns null
        val result = repo.getUser()
        assertNull(result)
    }
}

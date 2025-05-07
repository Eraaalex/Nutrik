package com.hse.coursework.nutrik.viewmodel

import com.google.firebase.auth.FirebaseAuth
import com.hse.coursework.nutrik.model.Restriction
import com.hse.coursework.nutrik.model.dto.Gender
import com.hse.coursework.nutrik.model.dto.User
import com.hse.coursework.nutrik.repository.user.UserRepository
import com.hse.coursework.nutrik.ui.theme.screen.account.AccountViewModel
import io.mockk.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.*
import org.junit.*
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue

@OptIn(ExperimentalCoroutinesApi::class)
class AccountViewModelTest {

    private val userRepo: UserRepository = mockk(relaxed = true)
    private lateinit var viewModel: AccountViewModel
    private val testDispatcher = StandardTestDispatcher()

    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `init loads user from repository`() = runTest {
        val testUser = User(email = "alice@gmail.com", age = 25)
        coEvery { userRepo.getUser() } returns testUser

        viewModel = AccountViewModel(userRepo)
        advanceUntilIdle()

        assertEquals("alice@gmail.com", viewModel.user.value.email)
        assertEquals(25, viewModel.user.value.age)
    }

    @Test
    fun `updateAge modifies age and saves user`() = runTest {
        val user = User(age = 20)
        coEvery { userRepo.getUser() } returns user
        coEvery { userRepo.saveUser(any()) } just Runs

        viewModel = AccountViewModel(userRepo)
        advanceUntilIdle()

        viewModel.updateAge(30)
        advanceUntilIdle()

        assertEquals(30, viewModel.user.value.age)
        coVerify { userRepo.saveUser(match { it.age == 30 }) }
    }

    @Test
    fun `updateGender modifies gender and saves user`() = runTest {
        val user = User(gender = Gender.MALE)
        coEvery { userRepo.getUser() } returns user
        coEvery { userRepo.saveUser(any()) } just Runs

        viewModel = AccountViewModel(userRepo)
        advanceUntilIdle()

        viewModel.updateGender(Gender.FEMALE)
        advanceUntilIdle()

        assertEquals(Gender.FEMALE, viewModel.user.value.gender)
        coVerify { userRepo.saveUser(match { it.gender == Gender.FEMALE }) }
    }

    @Test
    fun `updateRestrictions replaces restrictions list`() = runTest {
        val user = User(restrictions = listOf(Restriction.LACTOSE))
        coEvery { userRepo.getUser() } returns user
        coEvery { userRepo.saveUser(any()) } just Runs

        viewModel = AccountViewModel(userRepo)
        advanceUntilIdle()

        val newRestrictions = listOf(Restriction.LACTOSE, Restriction.NUT)
        viewModel.updateRestrictions(newRestrictions)
        advanceUntilIdle()

        assertEquals(newRestrictions.toSet(), viewModel.user.value.restrictions.toSet())
        coVerify { userRepo.saveUser(match { it.restrictions.contains(Restriction.NUT) }) }
    }

    @Test
    fun `toggleRestriction adds restriction if not present`() = runTest {
        val user = User(restrictions = emptyList())
        coEvery { userRepo.getUser() } returns user
        coEvery { userRepo.saveUser(any()) } just Runs

        viewModel = AccountViewModel(userRepo)
        advanceUntilIdle()

        viewModel.toggleRestriction(Restriction.LACTOSE)
        advanceUntilIdle()

        assertTrue(viewModel.user.value.restrictions.contains(Restriction.LACTOSE))
    }

    @Test
    fun `toggleRestriction removes restriction if present`() = runTest {
        val user = User(restrictions = listOf(Restriction.LACTOSE))
        coEvery { userRepo.getUser() } returns user
        coEvery { userRepo.saveUser(any()) } just Runs

        viewModel = AccountViewModel(userRepo)
        advanceUntilIdle()

        viewModel.toggleRestriction(Restriction.LACTOSE)
        advanceUntilIdle()

        assertTrue(viewModel.user.value.restrictions.isEmpty())
    }

    @Test
    fun `logout calls FirebaseAuth and clears user cache`() {
        mockkStatic(FirebaseAuth::class)
        val mockAuth = mockk<FirebaseAuth>(relaxed = true)
        every { FirebaseAuth.getInstance() } returns mockAuth

        viewModel = AccountViewModel(userRepo)
        viewModel.logout()

        verify { mockAuth.signOut() }
        verify { userRepo.clearCache() }

        unmockkStatic(FirebaseAuth::class)
    }
}

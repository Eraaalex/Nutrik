@file:OptIn(ExperimentalCoroutinesApi::class)

package com.hse.coursework.nutrik.repository

import android.util.Log
import com.hse.coursework.nutrik.data.dao.ProgressDao
import com.hse.coursework.nutrik.model.ProgressItem
import com.hse.coursework.nutrik.repository.progress.ProgressRemoteDataSource
import com.hse.coursework.nutrik.repository.progress.ProgressRepositoryImpl
import com.hse.coursework.nutrik.repository.progress.WeekProgressResult
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
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import java.time.LocalDate

class ProgressRepositoryImplTest {

    private lateinit var repo: ProgressRepositoryImpl
    private val dao: ProgressDao = mockk(relaxed = true)
    private val remote: ProgressRemoteDataSource = mockk()

    private val testDispatcher = StandardTestDispatcher()

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
        mockkStatic(Log::class)
        every { Log.e(any(), any()) } returns 0

        repo = ProgressRepositoryImpl(dao, remote)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
        unmockkAll()
    }

    @Test
    fun `fetchInitialDataForLast3Days calls remote and saves to DAO`() = runTest {
        val uid = "user"
        val progressItem = ProgressItem(LocalDate.now(), 1, 1, 1, 1, 1, 1, 0)

        coEvery { remote.getProgressForDate(any(), any()) } returns progressItem
        coEvery { dao.insert(any()) } just Runs

        repo.fetchInitialDataForLast3Days(uid)

        coVerify(exactly = 3) {
            remote.getProgressForDate(uid, any())
            dao.insert(any())
        }
    }

    @Test
    fun `getProgressForDate returns from local if available`() = runTest {
        val uid = "user"
        val date = LocalDate.now()
        val entity = ProgressItem(date, 1, 1, 1, 1, 1, 1, 0).toEntity(uid, date.toString())

        every { dao.getByUserAndDate(uid, date.toString()) } returns flowOf(entity)

        val flow = repo.getProgressForDate(uid, date)
        val result = flow.first()

        assertEquals(entity.toDomain(), result)
    }

    @Test
    fun `getProgressForDate gets from remote if local is null`() = runTest {
        val uid = "user"
        val date = LocalDate.now()
        val remoteItem = ProgressItem(date, 2, 2, 2, 2, 2, 2, 1)

        every { dao.getByUserAndDate(uid, date.toString()) } returns flowOf(null)
        coEvery { remote.getProgressForDate(uid, date) } returns remoteItem
        coEvery { dao.insert(any()) } just Runs

        val result = repo.getProgressForDate(uid, date).first()

        assertEquals(remoteItem, result)
    }

    @Test
    fun `getProgressForDate returns empty when remote and local are null`() = runTest {
        val uid = "user"
        val date = LocalDate.now()

        every { dao.getByUserAndDate(uid, date.toString()) } returns flowOf(null)
        coEvery { remote.getProgressForDate(uid, date) } returns null

        val result = repo.getProgressForDate(uid, date).first()

        assertEquals(0, result?.calories)
        assertEquals(0, result?.violationsCount)
    }

    @Test
    fun `saveProgress inserts and saves to remote`() = runTest {
        val uid = "user"
        val item = ProgressItem(LocalDate.now(), 1, 1, 1, 1, 1, 1, 0)

        coEvery { dao.insert(any()) } just Runs
        coEvery { remote.saveProgressForDate(uid, item) } just Runs

        repo.saveProgress(uid, item)

        coVerify {
            dao.insert(any())
            remote.saveProgressForDate(uid, item)
        }
    }

    @Test
    fun `fetchInitialDataForLastWeek stores remote week data to DAO`() = runTest {
        val uid = "user"
        val progressItem = ProgressItem(LocalDate.now(), 1, 1, 1, 1, 1, 1, 0)
        val weekData = listOf(WeekProgressResult(LocalDate.now(), progressItem))

        coEvery { remote.getProgressForWeek(uid) } returns weekData
        coEvery { dao.insert(any()) } just Runs

        val result = repo.fetchInitialDataForLastWeek(uid)

        assertEquals(weekData, result)
        coVerify(exactly = 1) { dao.insert(any()) }
    }
}

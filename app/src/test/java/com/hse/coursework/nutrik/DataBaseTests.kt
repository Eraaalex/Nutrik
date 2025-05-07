package com.hse.coursework.nutrik

import com.hse.coursework.nutrik.data.dao.ProgressDao
import com.hse.coursework.nutrik.model.ProgressEntity
import io.mockk.coVerifySequence
import io.mockk.mockk
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import org.junit.Test
import java.time.LocalDate

@OptIn(ExperimentalCoroutinesApi::class)
class MainPopulateDatabaseTest {

    @Test
    fun `populateDatabase calls clearAll then inserts three entries`() = runTest {
        val dao = mockk<ProgressDao>(relaxed = true)

        val today = LocalDate.now()

        populateDatabase(dao)

        coVerifySequence {
            dao.clearAll()
            dao.insert(match<ProgressEntity> {
                it.userId == "testUserId1" &&
                        it.date == today.toString() &&
                        it.protein == 80 &&
                        it.calories == 2000
            })
            dao.insert(match<ProgressEntity> {
                it.userId == "testUserId1" &&
                        it.date == today.minusDays(1).toString() &&
                        it.protein == 90 &&
                        it.violationsCount == 1
            })
            dao.insert(match<ProgressEntity> {
                it.userId == "testUserId1" &&
                        it.date == today.minusDays(2).toString() &&
                        it.protein == 70 &&
                        it.violationsCount == 2
            })
        }
    }
}

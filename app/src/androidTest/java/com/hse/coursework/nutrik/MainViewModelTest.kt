package com.hse.coursework.nutrik

import com.hse.coursework.nutrik.data.AppDatabase
import com.hse.coursework.nutrik.repository.ProgressRepository
import com.hse.coursework.nutrik.screen.main.MainViewModel
import dagger.hilt.android.testing.HiltAndroidRule
import dagger.hilt.android.testing.HiltAndroidTest
import kotlinx.coroutines.runBlocking
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import javax.inject.Inject

@HiltAndroidTest
class MainViewModelTest {

    @get:Rule
    var hiltRule = HiltAndroidRule(this)

    @Inject
    lateinit var database: AppDatabase

    @Inject
    lateinit var repository: ProgressRepository

    private lateinit var viewModel: MainViewModel

    @Before
    fun setUp() {
        hiltRule.inject()
        viewModel = MainViewModel(repository)

        // Заполнение базы тестовыми данными
          runBlocking {
            populateDatabase(database.progressDao())
        }
    }

    @Test
    fun testProgressData() = runBlocking {
        val data = viewModel.progressData.value
        assert(data.isNotEmpty())
        assert(data[0].title == "Калории")
    }
}

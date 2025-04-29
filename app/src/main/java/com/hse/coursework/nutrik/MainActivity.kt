package com.hse.coursework.nutrik

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import com.hse.coursework.nutrik.data.AppDatabase
import com.hse.coursework.nutrik.data.dao.ProgressDao
import com.hse.coursework.nutrik.model.ProgressEntity
import dagger.hilt.android.AndroidEntryPoint
import java.time.LocalDate
import javax.inject.Inject


@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    @Inject
    lateinit var database: AppDatabase
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            NutrikApp()
        }


    }
}

suspend fun populateDatabase(progressDao: ProgressDao) {
    val today = LocalDate.now()


    val testData = listOf(
        ProgressEntity(
            userId = "testUserId1",
            date = today.toString(),
            protein = 80,
            fat = 60,
            carbs = 250,
            calories = 2000,
            sugar = 30,
            salt = 5,
            violationsCount = 0
        ),
        ProgressEntity(
            userId = "testUserId1",
            date = today.minusDays(1).toString(),
            protein = 90,
            fat = 70,
            carbs = 200,
            calories = 1900,
            sugar = 25,
            salt = 4,
            violationsCount = 1
        ),
        ProgressEntity(
            userId = "testUserId1",
            date = today.minusDays(2).toString(),
            protein = 70,
            fat = 55,
            carbs = 270,
            calories = 2100,
            sugar = 35,
            salt = 6,
            violationsCount = 2
        )
    )

    progressDao.clearAll()

    testData.forEach { entity ->
        progressDao.insert(entity)
    }
}




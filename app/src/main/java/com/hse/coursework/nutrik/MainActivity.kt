package com.hse.coursework.nutrik

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.lifecycle.lifecycleScope
import com.google.firebase.firestore.FirebaseFirestore
import com.hse.coursework.nutrik.data.AppDatabase
import com.hse.coursework.nutrik.model.Product
import com.hse.coursework.nutrik.model.ProgressItem
import com.hse.coursework.nutrik.model.dao.ProgressDao
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
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

        lifecycleScope.launch {
            populateDatabase(database.progressDao())
        }

    }
}
suspend fun populateDatabase(progressDao: ProgressDao) {
    val testData = listOf(
        ProgressItem(id = "1", title = "Калории", progress = 0.7f),
        ProgressItem(id = "2", title = "Белки", progress = 0.5f),
        ProgressItem(id = "3", title = "Жиры", progress = 0.6f),
        ProgressItem(id = "4", title = "Углеводы", progress = 0.4f)
    )

    progressDao.clearProgressData()
    progressDao.insertAll(testData)
}


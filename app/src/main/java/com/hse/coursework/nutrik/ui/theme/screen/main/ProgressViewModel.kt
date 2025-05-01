package com.hse.coursework.nutrik.ui.theme.screen.main

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FirebaseAuth
import com.hse.coursework.nutrik.model.Consumption
import com.hse.coursework.nutrik.model.ProductEntity
import com.hse.coursework.nutrik.model.ProgressItem
import com.hse.coursework.nutrik.model.dto.User
import com.hse.coursework.nutrik.model.toUI
import com.hse.coursework.nutrik.repository.chat.ChatRepository
import com.hse.coursework.nutrik.repository.consumption.ConsumptionRepository
import com.hse.coursework.nutrik.repository.product.ProductRepository
import com.hse.coursework.nutrik.repository.progress.ProgressRepository
import com.hse.coursework.nutrik.repository.progress.ProgressService
import com.hse.coursework.nutrik.repository.progress.WeekProgressResult
import com.hse.coursework.nutrik.repository.user.UserRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.time.LocalDate
import javax.inject.Inject

@HiltViewModel
class ProgressViewModel @Inject constructor(
    private val repository: ProgressRepository,
    private val progressService: ProgressService,
    private val productRepository: ProductRepository,
    private val firebaseAuth: FirebaseAuth,
    private val userRepository: UserRepository,
    private val chatRepository: ChatRepository,
    private val consumptionRepository: ConsumptionRepository,
) :
    ViewModel() {
    private val _selectedDate = MutableStateFlow(LocalDate.now())
    private val _weekProgress = MutableStateFlow<List<WeekProgressResult>>(emptyList())
    private val _neuroAdvice = MutableStateFlow<String?>(null)
    private var userPrefs: User? = null

    val weekProgress: StateFlow<List<WeekProgressResult>> = _weekProgress
    val neuroAdvice: StateFlow<String?> = _neuroAdvice


    val _weeklyConsumption = MutableStateFlow<List<Consumption>>(emptyList())
    val weeklyConsumption: StateFlow<List<Consumption>> = _weeklyConsumption


    val uiState: StateFlow<ProgressUiState> = _selectedDate
        .flatMapLatest { date ->
            val uid = firebaseAuth.currentUser?.uid ?: ""
            repository.getProgressForDate(uid, date)
                .map { item -> ProgressUiState(selectedDate = date, progress = item) }
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.Lazily,
            initialValue = ProgressUiState()
        )

    fun onSelectDate(newDate: LocalDate) {
        _selectedDate.value = newDate
    }

    fun fetchInitialWeekDataAndTip() {

        val uid = firebaseAuth.currentUser?.uid ?: return
        viewModelScope.launch {
            _neuroAdvice.value = "Загрузка совета..."

            val startDate = LocalDate.now().minusDays(6)
            val endDate = LocalDate.now()
            val weeklyConsumption =
                consumptionRepository.getConsumptionForWeek(uid, startDate, endDate, viewModelScope)
            _weeklyConsumption.value = weeklyConsumption


            val weekData = repository.fetchInitialDataForLastWeek(uid)
            _weekProgress.value = weekData

            launch {
                userPrefs = userRepository.getUser()
                val todayCount = weekData.count { it.date == _selectedDate.value }
                Log.e("ProgressViewModel", "Today Count: $todayCount")
                val tip = chatRepository.fetchAdviceIfNeeded(weekData, userPrefs, uid, todayCount)
                _neuroAdvice.value = tip
            }
        }
    }

    private fun refreshWeeklyConsumption() {
        val uid = firebaseAuth.currentUser?.uid ?: return
        viewModelScope.launch {
            val today = LocalDate.now()
            val weekStart = today.minusDays(6)
            val weekData = consumptionRepository.getConsumptionForWeek(
                uid, weekStart, today, viewModelScope
            )
            _weeklyConsumption.value = weekData
        }
    }

    fun updateConsumption(entry: Consumption, newWeight: Double) {
        viewModelScope.launch {
            consumptionRepository.updateConsumption(entry, newWeight)
            val uid = firebaseAuth.currentUser?.uid ?: ""
            val product: ProductEntity? = productRepository.getById(entry.productId).firstOrNull()
            if (product != null) {
                val diffWeight = newWeight - entry.weight
                progressService.updateProgress(
                    product.toUI(),
                    diffWeight,
                    userId = uid,
                    user = userPrefs?.restrictions ?: emptyList(),
                )
            }
            refreshWeeklyConsumption()
        }
    }

    fun deleteConsumption(entry: Consumption) {
        viewModelScope.launch {
            consumptionRepository.deleteConsumption(entry)
            val uid = firebaseAuth.currentUser?.uid ?: ""
            val product: ProductEntity? = productRepository.getById(entry.productId).firstOrNull()
            if (product != null) {
                Log.e("ProgressViewModel", "Deleting consumption for product: ${entry}")
                Log.e("ProgressViewModel", "Deleting product: ${product.toUI()}")
                progressService.updateProgress(
                    product.toUI(),
                    newWeight = -entry.weight,
                    userId = uid,
                    user = userPrefs?.restrictions ?: emptyList(),
                    date = entry.date
                )
            }
            refreshWeeklyConsumption()
        }
    }

}


data class ProgressUiState(
    val selectedDate: LocalDate = LocalDate.now(),
    val progress: ProgressItem? = null

)


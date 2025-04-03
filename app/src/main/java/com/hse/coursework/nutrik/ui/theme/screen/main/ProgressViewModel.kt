package com.hse.coursework.nutrik.ui.theme.screen.main

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FirebaseAuth
import com.hse.coursework.nutrik.model.Consumption
import com.hse.coursework.nutrik.model.ProgressItem
import com.hse.coursework.nutrik.repository.chat.ChatRepository
import com.hse.coursework.nutrik.repository.consumption.ConsumptionRepository
import com.hse.coursework.nutrik.repository.progress.ProgressRepository
import com.hse.coursework.nutrik.repository.progress.WeekProgressResult
import com.hse.coursework.nutrik.repository.user.UserRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.time.LocalDate
import javax.inject.Inject

@HiltViewModel
class ProgressViewModel @Inject constructor(
    private val repository: ProgressRepository,
    private val firebaseAuth: FirebaseAuth,
    private val userRepository: UserRepository,
    private val chatRepository: ChatRepository,
    private val consumptionRepository: ConsumptionRepository,
) :
    ViewModel() {
    private val _selectedDate = MutableStateFlow(LocalDate.now())
    private val _weekProgress = MutableStateFlow<List<WeekProgressResult>>(emptyList())
    private val _neuroAdvice = MutableStateFlow<String?>(null)

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
                val userPrefs = userRepository.getUser()
                val message =
                    "Мой рацион за неделю: $weekData. Учитывай: $userPrefs. Дай краткие советы по питанию не более 2-3 советов, без нумерации и выделителей, не более 70 слов и мотивирующе."
                val tip = chatRepository.sendMessage(message)
                _neuroAdvice.value = tip
            }
        }
    }

}


data class ProgressUiState(
    val selectedDate: LocalDate = LocalDate.now(),
    val progress: ProgressItem? = null

)


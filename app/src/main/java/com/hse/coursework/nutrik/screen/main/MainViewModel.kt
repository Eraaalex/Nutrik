package com.hse.coursework.nutrik.screen.main

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.hse.coursework.nutrik.model.ProgressItem
import com.hse.coursework.nutrik.repository.ProgressRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import javax.inject.Inject

@HiltViewModel
class MainViewModel @Inject constructor(
    private val repository: ProgressRepository
) : ViewModel() {

    val progressData: StateFlow<List<ProgressItem>> = repository.getProgressData()
        .stateIn(viewModelScope, SharingStarted.Lazily, emptyList())
}

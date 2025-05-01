package com.hse.coursework.nutrik.ui.theme.screen.camera

import android.graphics.Bitmap
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.hse.coursework.nutrik.model.camera.CameraScanResult
import com.hse.coursework.nutrik.service.BarcodeScannerService
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class CameraViewModel @Inject constructor(
    private val analyzer: BarcodeScannerService
) : ViewModel() {

    var uiState by mutableStateOf(CameraUiState())
        private set

    fun onImageCaptured(bitmap: Bitmap) {
        uiState = uiState.copy(
            hasPhoto = true,
            photoBitmap = bitmap,
            scanning = true,
            message = null,
            foundProduct = null
        )

        viewModelScope.launch {
            when (val result = analyzer.analyze(bitmap)) {
                is CameraScanResult.Found -> {
                    uiState = uiState.copy(
                        scanning = false,
                        foundProduct = result.product
                    )
                }

                is CameraScanResult.NotFound -> {
                    uiState = uiState.copy(
                        scanning = false,
                        foundProduct = null,
                        message = result.message
                    )
                }

                is CameraScanResult.Error -> {
                    uiState = uiState.copy(
                        scanning = false,
                        message = result.message
                    )
                }
            }
        }
    }

    fun onRetake() {
        uiState = CameraUiState()
    }
}

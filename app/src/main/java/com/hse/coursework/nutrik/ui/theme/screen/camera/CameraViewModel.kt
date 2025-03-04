package com.hse.coursework.nutrik.ui.theme.screen.camera

import android.graphics.Bitmap
import android.net.Uri
import android.util.Log
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.mlkit.vision.barcode.BarcodeScanning
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.text.TextRecognition
import com.google.mlkit.vision.text.latin.TextRecognizerOptions
import com.hse.coursework.nutrik.repository.product.ProductRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import javax.inject.Inject

@HiltViewModel
class CameraViewModel @Inject constructor(
    private val productRepository: ProductRepository
) : ViewModel() {

    // Состояние экрана камеры
    var uiState by mutableStateOf(CameraUiState())
        private set

    // Инициализация клиентов ML Kit
    private val barcodeScanner = BarcodeScanning.getClient()
    private val textRecognizer = TextRecognition.getClient(TextRecognizerOptions.DEFAULT_OPTIONS)

    // Вызывается, когда сделано фото
    fun onImageCaptured(bitmap: Bitmap) {
        uiState = uiState.copy(
            hasPhoto = true,
            photoBitmap = bitmap,
            scanning = true,
            message = null,
            foundProduct = null
        )
        analyzeImage(bitmap)
    }

    private fun analyzeImage(bitmap: Bitmap) {
        val image = InputImage.fromBitmap(bitmap, 0)
        // Сначала пытаемся распознать штрих-код
        barcodeScanner.process(image)
            .addOnSuccessListener { barcodes ->
                if (barcodes.isNotEmpty()) {
                    val code = barcodes[0].rawValue
                    if (code != null) {
                        // Запускаем suspend‑функцию через viewModelScope.launch
                        viewModelScope.launch {
                            searchProductByCodeSuspend(code)
                        }
                        return@addOnSuccessListener
                    }
                }
                // Если штрих-код не найден или равен null, запускаем распознавание текста
                recognizeText(image)
            }
            .addOnFailureListener {
                // В случае ошибки сканера штрих-кодов — пробуем текстовое распознавание
                recognizeText(image)
            }
    }

    private suspend fun searchProductByCodeSuspend(code: String) {
        // Сначала проверяем и «очищаем» штрих-код
        val sanitizedCode = sanitizeBarcode(code)
        Log.d("CameraViewModel", "Sanitized barcode: $sanitizedCode")
        // Выполняем поиск продукта по штрих-коду в диспатчере IO
        val product = withContext(Dispatchers.IO) {
            productRepository.getProductByBarcode(sanitizedCode).firstOrNull()
        }
        uiState = if (product != null) {
            uiState.copy(scanning = false, foundProduct = product)
        } else {
            uiState.copy(scanning = false, message = "Продукт не найден по штрих-коду")
        }
    }

    /**
     * Функция для «очистки» штрих-кода. Если штрихкод выглядит как URL,
     * попробуем извлечь из него параметр RegNumber, иначе возвращаем исходную строку.
     */
    private fun sanitizeBarcode(code: String): String {
        return try {
            val uri = Uri.parse(code)
            // Если присутствуют схема и хост, значит, это URL.
            if (uri.scheme != null && uri.host != null) {
                // Извлекаем параметр "RegNumber"
                uri.getQueryParameter("RegNumber")?.trim() ?: code
            } else {
                code
            }
        } catch (e: Exception) {
            code
        }
    }

    private fun recognizeText(image: InputImage) {
        textRecognizer.process(image)
            .addOnSuccessListener { visionText ->
                val fullText = visionText.text ?: ""
                // Здесь берём первую непустую строку как предполагаемое название
                val nameQuery = fullText.lines().firstOrNull { it.isNotBlank() } ?: fullText
                if (nameQuery.isNotBlank()) {
                    viewModelScope.launch {
                        searchProductByNameSuspend(nameQuery)
                    }
                } else {
                    onProductNotFound()
                }
            }
            .addOnFailureListener {
                uiState = uiState.copy(
                    scanning = false,
                    message = "Не удалось распознать товар, попробуйте снова"
                )
            }
    }

    private suspend fun searchProductByNameSuspend(name: String) {
        // Выполняем поиск по названию
        val product = withContext(Dispatchers.IO) {
            productRepository.getProductByName(name).firstOrNull()
        }
        uiState = if (product != null) {
            uiState.copy(scanning = false, foundProduct = product)
        } else {
            uiState.copy(scanning = false, message = "Продукт не найден по названию")
        }
    }

    private fun onProductNotFound() {
        uiState = uiState.copy(scanning = false, foundProduct = null, message = "Продукт не найден")
    }

    fun onRetake() {
        // Сбрасываем состояние для повторного снимка
        uiState = CameraUiState()
    }
}
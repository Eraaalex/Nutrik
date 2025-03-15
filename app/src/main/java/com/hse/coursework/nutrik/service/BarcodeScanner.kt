package com.hse.coursework.nutrik.service


import android.graphics.Bitmap
import android.net.Uri
import com.google.mlkit.vision.barcode.BarcodeScanning
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.text.TextRecognition
import com.google.mlkit.vision.text.latin.TextRecognizerOptions
import com.hse.coursework.nutrik.model.ProductEntity
import com.hse.coursework.nutrik.model.camera.CameraScanResult
import com.hse.coursework.nutrik.repository.product.ProductRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import javax.inject.Inject


class BarcodeScanner @Inject constructor(
    private val productRepository: ProductRepository
) {
    private val barcodeScanner = BarcodeScanning.getClient()
    private val textRecognizer = TextRecognition.getClient(TextRecognizerOptions.DEFAULT_OPTIONS)

    suspend fun analyze(bitmap: Bitmap): CameraScanResult {
        val image = InputImage.fromBitmap(bitmap, 0)

        val barcode = try {
            val barcodes = barcodeScanner.process(image).await()
            barcodes.firstOrNull()?.rawValue
        } catch (e: Exception) {
            null
        }

        barcode?.let {
            val product = searchProductByCode(it)
            if (product != null) {
                return CameraScanResult.Found(product)
            }
        }

        val fullText = try {
            val visionText = textRecognizer.process(image).await()
            visionText.text
        } catch (e: Exception) {
            return CameraScanResult.Error("Не удалось распознать товар, попробуйте снова")
        }

        val nameQuery = fullText.lines().firstOrNull { it.isNotBlank() } ?: fullText
        if (nameQuery.isNotBlank()) {
            val product = searchProductByName(nameQuery)
            return product?.let { CameraScanResult.Found(it) }
                ?: CameraScanResult.NotFound("Продукт не найден по названию")
        }

        return CameraScanResult.NotFound("Продукт не найден")
    }

    private suspend fun searchProductByCode(code: String): ProductEntity? {
        val sanitizedCode = sanitizeBarcode(code)
        return withContext(Dispatchers.IO) {
            productRepository.getProductByBarcode(sanitizedCode).firstOrNull()
        }
    }

    private suspend fun searchProductByName(name: String): ProductEntity? {
        return withContext(Dispatchers.IO) {
            productRepository.getProductByName(name).firstOrNull()
        }
    }

    private fun sanitizeBarcode(code: String): String {
        return try {
            val uri = Uri.parse(code)
            if (uri.scheme != null && uri.host != null) {
                uri.getQueryParameter("RegNumber")?.trim() ?: code
            } else code
        } catch (e: Exception) {
            code
        }
    }
}

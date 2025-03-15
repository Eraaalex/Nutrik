package com.hse.coursework.nutrik.ui.theme.screen.camera

import android.graphics.Bitmap
import com.hse.coursework.nutrik.model.ProductEntity

data class CameraUiState(
    val hasPhoto: Boolean = false,
    val photoBitmap: Bitmap? = null,
    val scanning: Boolean = false,
    val foundProduct: ProductEntity? = null,
    val message: String? = null
)

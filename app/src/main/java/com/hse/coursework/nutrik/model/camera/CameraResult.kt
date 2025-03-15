package com.hse.coursework.nutrik.model.camera

import com.hse.coursework.nutrik.model.ProductEntity

sealed class CameraScanResult {
    data class Found(val product: ProductEntity) : CameraScanResult()
    data class NotFound(val message: String) : CameraScanResult()
    data class Error(val message: String) : CameraScanResult()
}

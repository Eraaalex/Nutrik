package com.hse.coursework.nutrik.model

sealed class ProductState {
    object Loading : ProductState()
    data class Success(val product: ProductEntity) : ProductState()
    data class Error(val message: String) : ProductState()
}


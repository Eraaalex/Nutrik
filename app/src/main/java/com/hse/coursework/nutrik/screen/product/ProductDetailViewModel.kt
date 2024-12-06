package com.hse.coursework.nutrik.screen.product

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FirebaseAuth
import com.hse.coursework.nutrik.model.Product
import com.hse.coursework.nutrik.model.ProductState
import com.hse.coursework.nutrik.repository.ConsumptionRepository
import com.hse.coursework.nutrik.repository.ProductRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ProductDetailViewModel @Inject constructor(
    private val repository: ProductRepository,
    private val consumptionRepository: ConsumptionRepository,
    private val firebaseAuth: FirebaseAuth
) : ViewModel() {
    val currentUser = firebaseAuth.currentUser

    private val _productState = MutableStateFlow<ProductState>(ProductState.Loading)
    val productState: StateFlow<ProductState> = _productState.asStateFlow()

    private val _isFavorite = MutableStateFlow(false)
    val isFavorite: StateFlow<Boolean> = _isFavorite.asStateFlow()

    fun updateConsumption(product: Product, newWeight: Double) {
        viewModelScope.launch {
            consumptionRepository.updateConsumption(
                product,
                newWeight,
                userId = currentUser?.uid ?: ""
            )
        }
    }

    fun loadProductById(productId: String) {
        viewModelScope.launch {
            repository.getById(productId)
                .onStart {
                    _productState.value = ProductState.Loading
                }
                .catch { exception ->
                    _productState.value = ProductState.Error(exception.message ?: "Unknown error")
                }
                .collect { product ->
                    _productState.value = ProductState.Success(product)
                    Log.e("ProductDetailViewModel", "Product loaded: $product")
                }
            repository.isFavorite(productId).collect { fav ->
                _isFavorite.value = fav
            }
        }
    }

    fun onFavoriteClick(product: Product) {
        val newValue = !_isFavorite.value
        _isFavorite.value = newValue

        viewModelScope.launch {
            repository.toggleFavorite(product, userId = currentUser?.uid ?: "")
        }
    }

}

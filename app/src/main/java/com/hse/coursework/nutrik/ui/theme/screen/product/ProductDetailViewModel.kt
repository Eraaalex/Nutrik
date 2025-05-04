package com.hse.coursework.nutrik.ui.theme.screen.product

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FirebaseAuth
import com.hse.coursework.nutrik.model.Product
import com.hse.coursework.nutrik.model.ProductEntity
import com.hse.coursework.nutrik.model.ProductState
import com.hse.coursework.nutrik.model.Restriction
import com.hse.coursework.nutrik.model.toUI
import com.hse.coursework.nutrik.repository.consumption.ConsumptionRepository
import com.hse.coursework.nutrik.repository.product.ProductRepository
import com.hse.coursework.nutrik.repository.progress.ProgressService
import com.hse.coursework.nutrik.repository.user.UserRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.launch
import java.time.LocalDate
import javax.inject.Inject

@HiltViewModel
class ProductDetailViewModel @Inject constructor(
    private val repository: ProductRepository,
    private val consumptionRepository: ConsumptionRepository,
    private val progressService: ProgressService,
    private val firebaseAuth: FirebaseAuth,
    private val userRepository: UserRepository
) : ViewModel() {
    val currentUser = firebaseAuth.currentUser

    private val _productState = MutableStateFlow<ProductState>(ProductState.Loading)
    val productState: StateFlow<ProductState> = _productState.asStateFlow()

    var _product: Product? = null;

    private val _isFavorite = MutableStateFlow(false)
    val isFavorite: StateFlow<Boolean> = _isFavorite.asStateFlow()

    private val _userRestrictions = MutableStateFlow(emptyList<Restriction>())

    fun updateConsumption(product: ProductEntity, newWeight: Double, date :LocalDate = LocalDate.now()) {
        viewModelScope.launch {
            if (newWeight <= 0) {
                Log.e("ProductDetailViewModel", "Invalid weight: $newWeight")
                return@launch
            }
            consumptionRepository.updateConsumption(
                product,
                newWeight,
                userId = currentUser?.uid ?: "",
                date = date
            )
            progressService.updateProgress(
                product.toUI(),
                newWeight,
                userId = currentUser?.uid ?: "",
                user = _userRestrictions.value,
                date = date
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
                    _productState.value =
                        ProductState.Error(exception.message ?: "Неизвестная ошибка")
                }
                .collect { product ->
                    if (product != null) {
                        _productState.value = ProductState.Success(product)
                        _product = product.toUI()
                        Log.e("ProductDetailViewModel", "Product loaded: $product")
                        Log.e("ProductDetailViewModel", "Product State loaded: ${_productState.value}")
                    } else {
                        _productState.value = ProductState.Error("Продукт не найден")
                    }
                }
            repository.isFavorite(productId).collect { fav ->
                _isFavorite.value = fav
            }
        }
    }

    fun onFavoriteClick(product: ProductEntity) {
        val newValue = !_isFavorite.value
        _isFavorite.value = newValue

        viewModelScope.launch {
            repository.toggleFavorite(product, userId = currentUser?.uid ?: "")
        }
    }

    fun fetchUserRestrictions() {
        viewModelScope.launch {
            _userRestrictions.value = userRepository.getUser()?.restrictions ?: emptyList()
        }
    }

    fun getUserRestrictions(): List<Restriction> {
        return if (_product == null) {
            Log.e("ProductDetailViewModel", "Product is null, cannot filter restrictions")
            emptyList()
        } else {
            Log.e("ProductDetailViewModel", "Product is not null, filtering restrictions")
            _userRestrictions.value.filter {
                _product!!.allergens.contains(it)
            }
        }
    }
}

package com.hse.coursework.nutrik.screen.favourite

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FirebaseAuth
import com.hse.coursework.nutrik.model.Product
import com.hse.coursework.nutrik.repository.ProductRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import javax.inject.Inject


@HiltViewModel
class FavouriteViewModel @Inject constructor(
    private val repository: ProductRepository,
    private val firebaseAuth: FirebaseAuth
) : ViewModel() {
    val currentUser = firebaseAuth.currentUser


    private val _productList = MutableStateFlow<List<Product>>(emptyList())
    val productList: StateFlow<List<Product>> = _productList.asStateFlow()


    init {
        // Подписываемся на изменения в базе
        viewModelScope.launch {
            repository.getFavoriteProducts().collect { products ->
                _productList.value = products
            }
        }
    }

    private var favoriteIds: List<String> = emptyList()
    private var currentPage = 0
    private val pageSize = 10

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    fun loadProducts() {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                if (favoriteIds.isEmpty()) {
                   Log.e("FavouriteViewModel", "Fetching favorite ids " + favoriteIds.toString())
                   Log.e("FavouriteViewModel", "Fetching favorite ids " + currentUser?.uid.toString())
                    favoriteIds = repository.fetchAllFavoriteIds(currentUser?.uid ?: "")
                }
                // Загружаем следующую страницу
                val newProducts = repository.fetchFavoritesByPage(favoriteIds, pageSize, currentPage).first()
                _productList.value = _productList.value + newProducts
                currentPage++
            } catch (e: Exception) {
                // Логируем или обрабатываем ошибки
                e.printStackTrace()
            } finally {
                _isLoading.value = false
            }
        }
    }
}


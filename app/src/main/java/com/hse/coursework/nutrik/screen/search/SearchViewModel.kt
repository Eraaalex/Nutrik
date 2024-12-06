package com.hse.coursework.nutrik.screen.search

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.firestore.DocumentSnapshot
import com.hse.coursework.nutrik.model.Product
import com.hse.coursework.nutrik.repository.LocalDataSource
import com.hse.coursework.nutrik.repository.RemoteDataSource
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SearchViewModel @Inject constructor(
    private val localDataSource: LocalDataSource,
    private val remoteDataSource: RemoteDataSource
) : ViewModel() {

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery

    private val _productList = MutableStateFlow<List<Product>>(emptyList())
    val productList: StateFlow<List<Product>> = _productList

    private var lastDocument: DocumentSnapshot? = null
    private val pageSize = 30
    private var isLoading = false
    private var hasMore = true

    init {
        loadProducts(reset = true)
    }

    fun updateSearchQuery(query: String) {
        _searchQuery.value = query
        loadProducts(reset = true)
    }

    fun loadProducts(reset: Boolean = false) {
        if (isLoading || !hasMore) return

        isLoading = true
        viewModelScope.launch {
            try {
                if (reset) {
                    lastDocument = null
                    _productList.value = emptyList()
                    hasMore = true
                }

                val localOffset = (_productList.value.size)
                val localProducts = localDataSource.searchProducts(
                    query = _searchQuery.value,
                    limit = pageSize,
                    offset = localOffset
                )
                _productList.value = _productList.value + localProducts

                if (localProducts.size < pageSize) {
                    val paginatedResult = remoteDataSource.fetchProductData(
                        limit = pageSize - localProducts.size,
                        startAfter = lastDocument,
                        query = _searchQuery.value.takeIf { it.isNotBlank() }
                    ).first()
                    localDataSource.saveProductData(paginatedResult.data)
                    _productList.value = _productList.value + paginatedResult.data
                    lastDocument = paginatedResult.lastDocument
                    if (paginatedResult.data.size < (pageSize - localProducts.size)) {
                        hasMore = false
                    }
                }
            } catch (e: Exception) {
                Log.e("SearchViewModel", "Error loading products", e)
            } finally {
                isLoading = false
            }
        }
    }

    fun loadNextPage() {
        if (isLoading || !hasMore) return
        loadProducts(reset = false)
    }
}


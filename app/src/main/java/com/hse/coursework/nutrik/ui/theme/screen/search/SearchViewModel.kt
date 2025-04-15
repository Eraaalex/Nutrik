package com.hse.coursework.nutrik.ui.theme.screen.search

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.firestore.DocumentSnapshot
import com.hse.coursework.nutrik.model.ProductEntity
import com.hse.coursework.nutrik.model.Restriction
import com.hse.coursework.nutrik.model.toUI
import com.hse.coursework.nutrik.repository.LocalDataSource
import com.hse.coursework.nutrik.repository.RemoteDataSource
import com.hse.coursework.nutrik.repository.user.UserRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SearchViewModel @Inject constructor(
    private val localDataSource: LocalDataSource,
    private val remoteDataSource: RemoteDataSource,
    private val userRepository: UserRepository
) : ViewModel() {

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery

    private val _productList = MutableStateFlow<List<ProductEntity>>(emptyList())
    val productList: StateFlow<List<ProductEntity>> = _productList

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading

    private val _userRestrictions = MutableStateFlow(emptyList<Restriction>())

    // Для постраничной подгрузки
    private var lastDocument: DocumentSnapshot? = null
    private val pageSize = 30
    private var hasMore = true

    // Переключатель режимов
    private val isSearchMode: Boolean
        get() = _searchQuery.value.isNotBlank()


    fun loadProducts(reset: Boolean = false) {
        // защитная проверка
        if (_isLoading.value || !hasMore) return

        _isLoading.value = true
        viewModelScope.launch {
            try {
                if (reset) {
                    lastDocument = null
                    _productList.value = emptyList()
                    hasMore = true
                }

                // 1) сначала попытка из локальной БД
                val offset = _productList.value.size
                val localPage = localDataSource.searchProducts(
                    query = _searchQuery.value,
                    limit = pageSize,
                    offset = offset
                )

                // 2) из remote, если локально мало
                val need = pageSize - localPage.size
                val remotePage = if (need > 0) {
                    val result = remoteDataSource.fetchProductData(
                        limit = need,
                        startAfter = lastDocument,
                        query = _searchQuery.value.takeIf { it.isNotBlank() }
                    ).first()
                    lastDocument = result.lastDocument
                    if (result.data.size < need) hasMore = false
                    localDataSource.saveProductData(result.data)
                    result.data
                } else emptyList()

                // 3) объединяем и фильтруем дубли по id
                val merged = (_productList.value + localPage + remotePage)
                    .distinctBy { it.id }

                _productList.value = merged

            } catch (e: Exception) {
                Log.e("SearchViewModel", "Error loading products", e)
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun updateSearchQuery(query: String) {
        _searchQuery.value = query.trim()
        viewModelScope.launch {
            if (isSearchMode) {
                // удаляем старый список и сразу ищем по всему датасету
                performFullTextSearch(_searchQuery.value)
            } else {
                // строка очистилась — возвращаемся к алфавиту
                loadAlpha(reset = true)
            }
        }
    }

    /** Алфавитная (постраничная) загрузка */
    fun loadNextPage() {
        if (!isSearchMode) loadAlpha(reset = false)
    }

    private fun loadAlpha(reset: Boolean) {
        if (_isLoading.value || !hasMore) return

        _isLoading.value = true
        viewModelScope.launch {
            try {
                if (reset) {
                    lastDocument = null
                    hasMore = true
                    _productList.value = emptyList()
                }

                // 1) сначала из локальной БД по алфавиту
                val offset = _productList.value.size
                val local = localDataSource.getRecentProducts(limit = pageSize, offset = offset)

                // 2) если не хватило — дозаливка из Firebase
                val need = pageSize - local.size
                val remote = if (need > 0) {
                    val page = remoteDataSource.fetchProductData(
                        limit = need,
                        startAfter = lastDocument,
                        query = null  // без фильтра
                    ).first()
                    lastDocument = page.lastDocument
                    if (page.data.size < need) hasMore = false
                    localDataSource.saveProductData(page.data)
                    page.data
                } else emptyList()

                // 3) объединяем и фильтруем дубли (distinctBy id)
                _productList.value =
                    (_productList.value + local + remote).distinctBy { it.id }

            } catch (e: Throwable) {
                Log.e("SearchVM", "alpha load failed", e)
            } finally {
                _isLoading.value = false
            }
        }
    }

    /** Поиск по всему датасету (локально + удалённо) */
    private suspend fun performFullTextSearch(query: String) {
        // 1) сбрасываем любую пагинацию
        lastDocument = null
        hasMore = false
        _isLoading.value = true

        // 2) сначала быстрый поиск в локальной БД (LIKE %query% или FTS)
        val localMatches =
            localDataSource.searchProducts(query = query, limit = Int.MAX_VALUE, offset = 0)
        _productList.value = localMatches

        val remoteMatches = remoteDataSource.searchProducts(query = query)
            .first()    // возвращает List<ProductEntity>
        Log.e("SearchViewModel", "Remote matches: $remoteMatches")
        // кэшируем и объединяем без дублей
        localDataSource.saveProductData(remoteMatches)
        _productList.value = (localMatches + remoteMatches)
            .distinctBy { it.id }

        _isLoading.value = false
    }

    fun fetchUserRestrictions() {
        viewModelScope.launch {
            _userRestrictions.value = userRepository.getUser()?.restrictions ?: emptyList()
        }
    }

    fun getUserRestrictions(product: ProductEntity): List<Restriction> {
        return _userRestrictions.value.filter {
            product.toUI().allergens.contains(it)
        }

    }

    fun isForbidden(product: ProductEntity): Boolean {
        return _userRestrictions.value.any { restriction ->
            product.toUI().allergens.contains(restriction)
        }
    }
}


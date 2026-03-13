package com.aristidevs.cursotestingandroid.productlist.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.aristidevs.cursotestingandroid.productlist.domain.model.Product
import com.aristidevs.cursotestingandroid.productlist.domain.model.ProductPromotion
import com.aristidevs.cursotestingandroid.productlist.domain.model.ProductWithPromotion
import com.aristidevs.cursotestingandroid.productlist.domain.model.PromotionType
import com.aristidevs.cursotestingandroid.productlist.domain.model.SortOption
import com.aristidevs.cursotestingandroid.productlist.domain.repository.SettingsRepository
import com.aristidevs.cursotestingandroid.productlist.domain.usecase.GetProductsUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ProductListViewModel @Inject constructor(
    private val getProductsUseCase: GetProductsUseCase,
    private val settingsRepository: SettingsRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow<ProductListUiState>(ProductListUiState.Loading)
    val uiState: StateFlow<ProductListUiState> = _uiState.asStateFlow()

    private val _events = MutableSharedFlow<ProductListEvent>(extraBufferCapacity = 1)
    val events: SharedFlow<ProductListEvent> = _events

    val filterVisible:StateFlow<Boolean> = settingsRepository.filtersVisible.stateIn(
        scope = viewModelScope,
        initialValue = true,
        started = SharingStarted.WhileSubscribed(5000)
    )

    private var productsJob: Job? = null

    init {
        loadProducts()
    }

    fun loadProducts() {
        _uiState.value = ProductListUiState.Loading
        productsJob?.cancel()
        productsJob = combine(
            getProductsUseCase(),
            settingsRepository.selectedCategory,
            settingsRepository.sortOption
        ) { products, category, sortOption ->
            var filteredProducts = products

            if (category != null) {
                filteredProducts = filteredProducts.filter { it.product.category == category }
            }

            val sorted = when (sortOption) {
                SortOption.PRICE_ASC -> filteredProducts.sortedBy { effectivePrice(it) }
                SortOption.PRICE_DESC -> filteredProducts.sortedByDescending { effectivePrice(it) }
                SortOption.NONE -> filteredProducts
                SortOption.DISCOUNT ->
//                    filteredProducts.sortedByDescending { effectiveDiscountPercent(it) }
                    filteredProducts.sortedWith(
                        compareByDescending<ProductWithPromotion> {
                            effectiveDiscountPercent(it)
                        }.thenBy { it.promotion == null}
                    )
            }

            val categories = products.map { it.product.category }.distinct().sorted()

            ProductListUiState.Success(
                products = sorted,
                categories = categories,
                selectedCategory = category,
                sortOption = sortOption
            )
        }.onEach { state ->
            _uiState.value = state
        }.catch { e: Throwable ->
            _uiState.value = ProductListUiState.Error(e.message.orEmpty())
        }.launchIn(viewModelScope)
    }

    fun setCategory(category: String?) {
        viewModelScope.launch {
            settingsRepository.setSelectedCategory(category)
        }
    }

    fun setSortOption(sortOption: SortOption) {
        viewModelScope.launch {
            settingsRepository.setSortOption(sortOption)
        }
    }

    fun setFilterVisible(showFilters: Boolean) {
        viewModelScope.launch {
            settingsRepository.setFiltersVisible(showFilters)
        }
    }

    private fun effectiveDiscountPercent(item: ProductWithPromotion): Double {
        return when (val promo = item.promotion) {
            is ProductPromotion.Percent -> promo.percent
            else -> 0.0
        }
    }

    private fun effectivePrice(item: ProductWithPromotion): Double {
        return when (val promo = item.promotion) {
            is ProductPromotion.Percent -> promo.discountedPrice
            else -> item.product.price
        }
    }
}
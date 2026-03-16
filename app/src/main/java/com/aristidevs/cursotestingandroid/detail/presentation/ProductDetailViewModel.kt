package com.aristidevs.cursotestingandroid.detail.presentation

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.navigation.toRoute
import com.aristidevs.cursotestingandroid.cart.domain.usecase.AddToCartUseCase
import com.aristidevs.cursotestingandroid.core.domain.model.AppError
import com.aristidevs.cursotestingandroid.core.domain.model.AppError.DatabaseError
import com.aristidevs.cursotestingandroid.core.domain.model.AppError.NetworkError
import com.aristidevs.cursotestingandroid.core.domain.model.AppError.NotFoundError
import com.aristidevs.cursotestingandroid.core.domain.model.AppError.UnknownError
import com.aristidevs.cursotestingandroid.core.domain.model.AppError.Validation
import com.aristidevs.cursotestingandroid.core.presentation.navigation.Screen
import com.aristidevs.cursotestingandroid.detail.domain.usecase.GetProductDetailWithPromotionUseCase
import com.aristidevs.cursotestingandroid.detail.presentation.ProductDetailEvent.SUCCESS_ADD_TO_CART
import dagger.assisted.Assisted
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.mapLatest
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel(assistedFactory = ProductDetailViewModel.Factory::class)
class ProductDetailViewModel @AssistedInject constructor(
    getProductDetailWithPromotionUseCase: GetProductDetailWithPromotionUseCase,
    private val addToCartUseCase: AddToCartUseCase,
    @Assisted val productId: String
) : ViewModel() {


    @OptIn(ExperimentalCoroutinesApi::class)
    val uiState =
        getProductDetailWithPromotionUseCase(productId).mapLatest { product ->
            ProductDetailUiState(item = product, isLoading = false)
        }.catch { e: Throwable ->
            if (e is AppError) {
                handleError(e)
            } else {
                handleError(UnknownError(e.message))
            }
            emit(ProductDetailUiState(isLoading = false))
        }.stateIn(
            scope = viewModelScope,
            initialValue = ProductDetailUiState(),
            started = SharingStarted.WhileSubscribed(5000)
        )

    private val _events = MutableSharedFlow<ProductDetailEvent>(extraBufferCapacity = 1)
    val events: SharedFlow<ProductDetailEvent> = _events


    fun addToCart(productId: String) {
        viewModelScope.launch {
            try {
                addToCartUseCase(productId)
                _events.emit(SUCCESS_ADD_TO_CART)
            } catch (e: AppError) {
                handleError(e)
            } catch (e: Exception) {
                handleError(UnknownError(e.message))
            }
        }
    }

    private suspend fun handleError(e: AppError) {
        val newEvent = when (e) {
            NetworkError() -> ProductDetailEvent.NETWORK_ERROR
            is Validation.InsufficientStock -> ProductDetailEvent.INSUFFICIENT_STOCK_ERROR
            is UnknownError, is DatabaseError, is NotFoundError, is Validation.QuantityMustBePositive -> ProductDetailEvent.UNKNOWN_ERROR
            else -> ProductDetailEvent.UNKNOWN_ERROR
        }
        _events.emit(newEvent)
    }

    @AssistedFactory
    interface Factory {
        fun create(productId: String): ProductDetailViewModel
    }
}
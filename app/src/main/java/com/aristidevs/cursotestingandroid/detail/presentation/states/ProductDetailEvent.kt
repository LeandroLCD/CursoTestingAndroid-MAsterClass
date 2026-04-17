package com.aristidevs.cursotestingandroid.detail.presentation.states

sealed interface ProductDetailEvent {

    data class Error(val cause: Throwable) : ProductDetailEvent
    data object INSUFFICIENT_STOCK_ERROR: ProductDetailEvent
    data object SUCCESS_ADD_TO_CART: ProductDetailEvent
}
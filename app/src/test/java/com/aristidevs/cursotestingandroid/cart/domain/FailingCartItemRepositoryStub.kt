package com.aristidevs.cursotestingandroid.cart.domain

import com.aristidevs.cursotestingandroid.cart.domain.model.CartItem
import com.aristidevs.cursotestingandroid.cart.domain.repository.CartItemRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf

class FailingCartItemRepositoryStub(private val exception: Throwable) : CartItemRepository {

    override fun getCartItems(): Flow<List<CartItem>> = flowOf(emptyList())

    override suspend fun addToCart(productId: String, quantity: Int) {
        throw exception
    }

    override suspend fun removeFromCart(productId: String) {
        throw exception
    }

    override suspend fun updateQuantity(productId: String, quantity: Int) {
        throw exception
    }

    override suspend fun clearCart() {
        throw exception
    }

    override suspend fun getCartItemById(productId: String): CartItem? = null
}


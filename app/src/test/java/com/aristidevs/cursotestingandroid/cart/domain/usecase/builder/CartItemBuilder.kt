package com.aristidevs.cursotestingandroid.cart.domain.usecase.builder

import com.aristidevs.cursotestingandroid.cart.domain.model.CartItem

class CartItemBuilder {
    private var productId: String = "product-1"
    private var quantity: Int = 2

    fun withProductId(productId: String) = apply { this.productId = productId }
    fun withQuantity(quantity: Int) = apply { this.quantity = quantity }

    fun build(): CartItem {
        return CartItem(productId, quantity)
    }
}
fun cartItem(block: CartItemBuilder.() -> Unit): CartItem = CartItemBuilder().apply(block).build()



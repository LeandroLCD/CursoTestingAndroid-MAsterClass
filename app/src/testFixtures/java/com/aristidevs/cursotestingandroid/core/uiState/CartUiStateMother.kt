package com.aristidevs.cursotestingandroid.core.uiState

import com.aristidevs.cursotestingandroid.cart.domain.model.CartItem
import com.aristidevs.cursotestingandroid.cart.domain.model.CartSummary
import com.aristidevs.cursotestingandroid.cart.presentation.CartUiState
import com.aristidevs.cursotestingandroid.cart.presentation.model.CartItemWithPromotion
import com.aristidevs.cursotestingandroid.core.builder.product
import com.aristidevs.cursotestingandroid.productlist.domain.model.ProductPromotion
import com.aristidevs.cursotestingandroid.productlist.domain.model.ProductWithPromotion

object CartUiStateMother {

    val defaultCartItems: List<CartItemWithPromotion> = listOf(
        CartItemWithPromotion(
            cartItem = CartItem(productId = "product-1", quantity = 2),
            item = ProductWithPromotion(
                product = product {
                    withId("product-1")
                    withName("Apple")
                    withPrice(1.50)
                    withStock(10)
                }
            )
        ),
        CartItemWithPromotion(
            cartItem = CartItem(productId = "product-2", quantity = 1),
            item = ProductWithPromotion(
                product = product {
                    withId("product-2")
                    withName("Orange")
                    withPrice(2.00)
                    withStock(5)
                }
            )
        )
    )

    val defaultCartItemWithDiscount: CartItemWithPromotion = CartItemWithPromotion(
        cartItem = CartItem(productId = "product-3", quantity = 1),
        item = ProductWithPromotion(
            product = product {
                withId("product-3")
                withName("Pear")
                withDescription("Discounted pear")
                withPrice(3.00)
                withStock(8)
            },
            promotion = ProductPromotion.Percent(percent = 20.0, discountedPrice = 2.40)
        )
    )
    val defaultCartItemWithOneStock: CartItemWithPromotion = CartItemWithPromotion(
        cartItem = CartItem(productId = "product-3", quantity = 1),
        item = ProductWithPromotion(
            product = product {
                withId("product-3")
                withName("Pear")
                withDescription("Discounted pear")
                withPrice(3.00)
                withStock(8)
            },
            promotion = ProductPromotion.Percent(percent = 20.0, discountedPrice = 2.40)
        )
    )

    val defaultSummary: CartSummary = CartSummary(
        subtotal = 5.00,
        discountTotal = 0.0,
        finalTotal = 5.00
    )

    val defaultSummaryWithDiscount: CartSummary = CartSummary(
        subtotal = 5.00,
        discountTotal = 1.00,
        finalTotal = 4.00
    )

    fun loading(): CartUiState.Loading = CartUiState.Loading

    fun error(message: String = "unknown error"): CartUiState.Error =
        CartUiState.Error(message = message)

    fun success(
        cartItems: List<CartItemWithPromotion> = defaultCartItems,
        summary: CartSummary? = defaultSummary,
        isLoading: Boolean = false
    ): CartUiState.Success = CartUiState.Success(
        cartItems = cartItems,
        summary = summary,
        isLoading = isLoading
    )

    fun successEmpty(
        summary: CartSummary? = null,
        isLoading: Boolean = false
    ): CartUiState.Success = CartUiState.Success(
        cartItems = emptyList(),
        summary = summary,
        isLoading = isLoading
    )
    fun successWithOneProduct(quantity: Int = 1, stock:Int = 8): CartUiState.Success = CartUiState.Success(
        cartItems = listOf(
            CartItemWithPromotion(
                cartItem = CartItem(productId = "product-3", quantity = quantity),
                item = ProductWithPromotion(
                    product = product {
                        withId("product-3")
                        withName("Pear")
                        withDescription("Discounted pear")
                        withPrice(3.00)
                        withStock(stock)
                    },
                    promotion = ProductPromotion.Percent(percent = 20.0, discountedPrice = 2.40)
                )
            )
        ),
        summary = defaultSummary,
        isLoading = false
    )
}


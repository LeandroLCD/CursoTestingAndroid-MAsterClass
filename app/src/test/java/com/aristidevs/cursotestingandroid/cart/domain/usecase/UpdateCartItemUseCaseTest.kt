package com.aristidevs.cursotestingandroid.cart.domain.usecase

import com.aristidevs.cursotestingandroid.cart.domain.repository.CartItemRepository
import com.aristidevs.cursotestingandroid.core.domain.model.AppError
import com.aristidevs.cursotestingandroid.productlist.domain.model.Product
import com.aristidevs.cursotestingandroid.productlist.domain.repository.ProductRepository
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test

class UpdateCartItemUseCaseTest {

    private lateinit var cartItemRepository: CartItemRepository
    private lateinit var productRepository: ProductRepository
    private lateinit var useCase: UpdateCartItemUseCase

    @Before
    fun setUp() {
        cartItemRepository = mockk(relaxed = true)
        productRepository = mockk()
        useCase = UpdateCartItemUseCase(cartItemRepository, productRepository)
    }

    @Test
    fun should_throw_QuantityMustBePositive_when_quantity_is_negative_in_invoke() = runTest {
        //GIVEN
        val productId = "1"
        val quantity = -1
        //WHEN
        val result = runCatching { useCase.invoke(productId, quantity) }

        //THEN
        assert(result.exceptionOrNull() is AppError.Validation.QuantityMustBePositive)
    }

    @Test
    fun should_remove_item_from_cart_when_quantity_is_zero_in_invoke() = runTest {
        //GIVEN
        val productId = "1"
        val quantity = 0

        //WHEN
        useCase.invoke(productId, quantity)

        //THEN
        coVerify { cartItemRepository.removeFromCart(productId) }
        coVerify(exactly = 0) { cartItemRepository.updateQuantity(any(), any()) }
    }

    @Test
    fun should_throw_NotFoundError_when_product_does_not_exist_in_invoke() = runTest {
        //GIVEN
        val productId = "1"
        val quantity = 2
        every { productRepository.getProductById(productId) } returns flowOf(null)

        //WHEN
        val result = runCatching { useCase.invoke(productId, quantity) }

        //THEN
        assert(result.exceptionOrNull() is AppError.NotFoundError)
    }

    @Test
    fun should_throw_InsufficientStock_when_quantity_exceeds_product_stock_in_invoke() = runTest {
        //GIVEN
        val productId = "1"
        val quantity = 10
        val product = Product(
            id = productId,
            name = "Product 1",
            description = "Description",
            price = 10.0,
            category = "Category",
            stock = 5
        )
        every { productRepository.getProductById(productId) } returns flowOf(product)

        //WHEN
        val result = runCatching { useCase.invoke(productId, quantity) }

        //THEN
        val exception = result.exceptionOrNull() as? AppError.Validation.InsufficientStock
        assert(exception != null)
        assertEquals(5, exception?.available)
    }

    @Test
    fun should_update_quantity_when_quantity_is_valid_and_stock_is_sufficient_in_invoke() = runTest {
        //GIVEN
        val productId = "1"
        val quantity = 3
        val product = Product(
            id = productId,
            name = "Product 1",
            description = "Description",
            price = 10.0,
            category = "Category",
            stock = 5
        )
        every { productRepository.getProductById(productId) } returns flowOf(product)

        //WHEN
        useCase.invoke(productId, quantity)

        //THEN
        coVerify { cartItemRepository.updateQuantity(productId, quantity) }
    }
}

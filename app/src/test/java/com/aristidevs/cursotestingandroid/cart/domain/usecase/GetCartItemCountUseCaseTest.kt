package com.aristidevs.cursotestingandroid.cart.domain.usecase

import com.aristidevs.cursotestingandroid.cart.domain.model.CartItem
import com.aristidevs.cursotestingandroid.cart.domain.repository.CartItemRepository
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test

class
GetCartItemCountUseCaseTest {

    private lateinit var cartItemRepository: CartItemRepository
    private lateinit var useCase: GetCartItemCountUseCase

    @Before
    fun setUp() {
        cartItemRepository = mockk()
        useCase = GetCartItemCountUseCase(cartItemRepository)
    }

    @Test
    fun returns_0_when_cart_is_empty() = runTest {
        every { cartItemRepository.getCartItems() } returns flowOf(emptyList())

        val result = useCase().first()

        assertEquals(0, result)
    }

    @Test
    fun returns_total_units_summing_all_item_quantities() = runTest {
        every { cartItemRepository.getCartItems() } returns flowOf(
            listOf(
                CartItem(productId = "p1", quantity = 2),
                CartItem(productId = "p2", quantity = 2),
                CartItem(productId = "p3", quantity = 2),
            )
        )

        val result = useCase().first()

        assertEquals(6, result)
    }

    @Test
    fun returns_correct_count_with_mixed_quantities() = runTest {
        every { cartItemRepository.getCartItems() } returns flowOf(
            listOf(
                CartItem(productId = "p1", quantity = 1),
                CartItem(productId = "p2", quantity = 3),
                CartItem(productId = "p3", quantity = 5),
            )
        )

        val result = useCase().first()

        assertEquals(9, result)
    }

    @Test
    fun returns_quantity_when_only_one_item_in_cart() = runTest {
        every { cartItemRepository.getCartItems() } returns flowOf(
            listOf(CartItem(productId = "p1", quantity = 4))
        )

        val result = useCase().first()

        assertEquals(4, result)
    }
}

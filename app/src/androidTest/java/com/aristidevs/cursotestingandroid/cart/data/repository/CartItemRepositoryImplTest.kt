package com.aristidevs.cursotestingandroid.cart.data.repository

import androidx.test.ext.junit.runners.AndroidJUnit4
import app.cash.turbine.test
import com.aristidevs.cursotestingandroid.cart.domain.repository.CartItemRepository
import com.aristidevs.cursotestingandroid.core.domain.model.AppError
import dagger.hilt.android.testing.HiltAndroidRule
import dagger.hilt.android.testing.HiltAndroidTest
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import javax.inject.Inject

@HiltAndroidTest
@RunWith(AndroidJUnit4::class)
class CartItemRepositoryImplTest {

    @get:Rule
    val hiltRule = HiltAndroidRule(this)

    @Inject
    lateinit var cartItemRepository: CartItemRepository

    @Before
    fun setUp() {
        hiltRule.inject()
        runBlocking { cartItemRepository.clearCart() }
    }

    // ─── getCartItems ─────────────────────────────────────────────────────────────

    @Test
    fun should_emit_empty_list_when_cart_is_empty_in_getCartItems() = runTest {

        //WHEN & THEN
        cartItemRepository.getCartItems().test {
            val items = awaitItem()
            assertEquals(0, items.size)
            cancelAndConsumeRemainingEvents()
        }
    }

    @Test
    fun should_emit_cart_items_when_cart_has_items_in_getCartItems() = runTest {
        //GIVEN
        cartItemRepository.addToCart("product-1", 2)
        cartItemRepository.addToCart("product-2", 3)

        //WHEN & THEN
        cartItemRepository.getCartItems().test {
            val items = awaitItem()
            assertEquals(2, items.size)
            assertTrue(items.any { it.productId == "product-1" && it.quantity == 2 })
            assertTrue(items.any { it.productId == "product-2" && it.quantity == 3 })
            cancelAndConsumeRemainingEvents()
        }
    }

    // ─── addToCart ────────────────────────────────────────────────────────────────

    @Test
    fun should_add_new_item_when_product_is_not_in_cart_in_addToCart() = runTest {
        //GIVEN
        val productId = "product-1"
        val quantity = 2

        //WHEN
        cartItemRepository.addToCart(productId, quantity)

        //THEN
        val item = cartItemRepository.getCartItemById(productId)
        assertNotNull(item)
        assertEquals(productId, item?.productId)
        assertEquals(quantity, item?.quantity)
    }

    @Test
    fun should_accumulate_quantity_when_product_already_exists_in_cart_in_addToCart() = runTest {
        //GIVEN
        val productId = "product-1"
        cartItemRepository.addToCart(productId, 2)

        //WHEN
        cartItemRepository.addToCart(productId, 3)

        //THEN
        val item = cartItemRepository.getCartItemById(productId)
        assertNotNull(item)
        assertEquals(5, item?.quantity)
    }

    // ─── removeFromCart ───────────────────────────────────────────────────────────

    @Test
    fun should_remove_item_when_it_exists_in_removeFromCart() = runTest {
        //GIVEN
        val productId = "product-1"
        cartItemRepository.addToCart(productId, 1)

        //WHEN
        cartItemRepository.removeFromCart(productId)

        //THEN
        val item = cartItemRepository.getCartItemById(productId)
        assertNull(item)
    }

    @Test
    fun should_throw_NotFoundError_when_item_does_not_exist_in_removeFromCart() = runTest {
        //GIVEN
        val productId = "non-existent"

        //WHEN
        val result = runCatching { cartItemRepository.removeFromCart(productId) }

        //THEN
        assertTrue(result.isFailure)
        assertTrue(result.exceptionOrNull() is AppError.NotFoundError)
    }

    // ─── updateQuantity ───────────────────────────────────────────────────────────

    @Test
    fun should_update_quantity_when_item_exists_in_updateQuantity() = runTest {
        //GIVEN
        val productId = "product-1"
        cartItemRepository.addToCart(productId, 1)
        val newQuantity = 5

        //WHEN
        cartItemRepository.updateQuantity(productId, newQuantity)

        //THEN
        val item = cartItemRepository.getCartItemById(productId)
        assertNotNull(item)
        assertEquals(newQuantity, item?.quantity)
    }

    @Test
    fun should_throw_NotFoundError_when_item_does_not_exist_in_updateQuantity() = runTest {
        //GIVEN
        val productId = "non-existent"

        //WHEN
        val result = runCatching { cartItemRepository.updateQuantity(productId, 3) }

        //THEN
        assertTrue(result.isFailure)
        assertTrue(result.exceptionOrNull() is AppError.NotFoundError)
    }

    // ─── clearCart ────────────────────────────────────────────────────────────────

    @Test
    fun should_remove_all_items_from_cart_in_clearCart() = runTest {
        //GIVEN
        cartItemRepository.addToCart("product-1", 2)
        cartItemRepository.addToCart("product-2", 1)
        cartItemRepository.addToCart("product-3", 4)

        //WHEN
        cartItemRepository.clearCart()

        //THEN
        cartItemRepository.getCartItems().test {
            val items = awaitItem()
            assertEquals(0, items.size)
            cancelAndConsumeRemainingEvents()
        }
    }

    // ─── getCartItemById ──────────────────────────────────────────────────────────

    @Test
    fun should_return_cart_item_when_it_exists_in_getCartItemById() = runTest {
        //GIVEN
        val productId = "product-1"
        val quantity = 3
        cartItemRepository.addToCart(productId, quantity)

        //WHEN
        val item = cartItemRepository.getCartItemById(productId)

        //THEN
        assertNotNull(item)
        assertEquals(productId, item?.productId)
        assertEquals(quantity, item?.quantity)
    }

    @Test
    fun should_return_null_when_item_does_not_exist_in_getCartItemById() = runTest {
        //GIVEN
        val productId = "non-existent"

        //WHEN
        val item = cartItemRepository.getCartItemById(productId)

        //THEN
        assertNull(item)
    }
}
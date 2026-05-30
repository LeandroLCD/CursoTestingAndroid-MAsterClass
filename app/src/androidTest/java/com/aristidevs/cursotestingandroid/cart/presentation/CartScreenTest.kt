package com.aristidevs.cursotestingandroid.cart.presentation

import android.annotation.SuppressLint
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertIsEnabled
import androidx.compose.ui.test.assertIsNotEnabled
import androidx.compose.ui.test.hasTestTag
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.onFirst
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollToNode
import androidx.compose.ui.test.performTouchInput
import androidx.compose.ui.test.swipeRight
import com.aristidevs.cursotestingandroid.R
import com.aristidevs.cursotestingandroid.core.BaseComposeTest
import com.aristidevs.cursotestingandroid.core.uiState.CartUiStateMother
import com.aristidevs.cursotestingandroid.productlist.domain.model.ProductPromotion
import org.junit.Assert.assertEquals
import org.junit.Test
import java.text.NumberFormat
import java.util.Currency.getInstance

class CartScreenTest : BaseComposeTest() {

    private fun renderScreen(
        state: CartUiState,
        onBack: () -> Unit = {},
        onIncreaseQuantity: (String, Int) -> Unit = { _, _ -> },
        onDecreaseQuantity: (String, Int) -> Unit = { _, _ -> },
        onRemove: (String) -> Unit = {}
    ) {
        composeRule.setContent {
            CartScreenContent(
                state = state,
                onBack = onBack,
                onIncreaseQuantity = onIncreaseQuantity,
                onDecreaseQuantity = onDecreaseQuantity,
                onRemove = onRemove
            )
        }
    }

    // ─── Loading ─────────────────────────────────────────────────────────────────

    @Test
    fun should_show_loading_indicator_when_state_is_loading_in_uiState() {
        //GIVEN
        val state = CartUiStateMother.loading()
        //WHEN
        renderScreen(state)
        //THEN
        composeRule.onNodeWithTag(idResources(R.id.cart_loading)).assertIsDisplayed()
    }

    // ─── Error ───────────────────────────────────────────────────────────────────

    @Test
    fun should_show_error_message_when_state_is_error_in_uiState() {
        //GIVEN
        val uiState = CartUiStateMother.error()
        //WHEN
        renderScreen(uiState)
        //THEN
        composeRule.onNodeWithTag(idResources(R.id.cart_state_error_content)).assertIsDisplayed()
        composeRule.onNodeWithText(stringResources(R.string.error_with_msj, uiState.message))
            .assertIsDisplayed()
    }

    @Test
    fun should_call_onBack_when_button_is_clicked_in_error_state_in_onBack() {
        //GIVEN
        val uiState = CartUiStateMother.error()
        var isClicked: Boolean? = null
        renderScreen(uiState, onBack = { isClicked = true })
        //WHEN
        composeRule.onNodeWithTag(idResources(R.id.cart_error_button)).performClick()

        //THEN
        assertEquals(true, isClicked)
    }

    // ─── Success — carrito vacío ──────────────────────────────────────────────────

    @Test
    fun should_show_empty_cart_message_when_cart_items_are_empty_in_uiState() {
        //GIVEN
        val uiState = CartUiStateMother.successEmpty()
        //WHEN
        renderScreen(uiState)
        //THEN
        composeRule.onNodeWithTag(idResources(R.id.cart_list_lasy)).assertDoesNotExist()
        composeRule.onNodeWithText(stringResources(R.string.car_icon)).assertIsDisplayed()
        composeRule.onNodeWithText(stringResources(R.string.car_is_empty)).assertIsDisplayed()
        composeRule.onNodeWithText(stringResources(R.string.add_product)).assertIsDisplayed()
    }


    @Test
    fun should_not_show_summary_when_cart_items_are_empty_in_uiState() {
        //GIVEN
        val uiState = CartUiStateMother.successEmpty()
        //WHEN
        renderScreen(uiState)
        //THEN
        composeRule.onNodeWithTag(idResources(R.id.cart_summary)).assertDoesNotExist()
    }

    // ─── Success — carrito con items ──────────────────────────────────────────────

    @Test
    fun should_show_product_name_when_cart_has_items_in_uiState() {
        //GIVEN
        val cartItems = CartUiStateMother.defaultCartItems
        val uiState = CartUiStateMother.success(cartItems)
        val itemFirst = cartItems.first()
        val itemSecond = cartItems.last()
        //WHEN
        renderScreen(uiState)
        //THEN
        composeRule.onNodeWithTag(idResources(R.id.cart_list_lasy)).assertIsDisplayed()
        composeRule.onNodeWithTag(idResources(R.id.cart_item_card, itemFirst.cartItem.productId))
            .assertIsDisplayed()
        composeRule.onNodeWithTag(idResources(R.id.cart_list_lasy)).performScrollToNode(
            hasTestTag(idResources(R.id.cart_item_card, itemSecond.cartItem.productId))
        ).assertIsDisplayed()
    }

    @Test
    fun should_show_unit_price_when_cart_item_has_no_promotion_in_uiState() {
        //GIVEN
        val cartItems = CartUiStateMother.defaultCartItems
        val uiState = CartUiStateMother.success(cartItems)
        val itemFirst = cartItems.first()
        val currencyFormatter = NumberFormat.getCurrencyInstance().apply {
            currency = getInstance("USD")
        }

        //WHEN
        renderScreen(uiState)

        //THEN
        composeRule.onNodeWithTag(idResources(R.id.cart_item_card, itemFirst.cartItem.productId))
            .assertIsDisplayed()
        composeRule.onAllNodesWithText(
            stringResources(R.string.c_u, currencyFormatter.format(itemFirst.item.product.price))
        ).onFirst().assertIsDisplayed()
    }

    @Test
    fun should_show_discounted_price_and_original_price_strikethrough_when_cart_item_has_percent_promotion_in_uiState() {
        //GIVEN
        val cartItem = CartUiStateMother.defaultCartItemWithDiscount
        val uiState = CartUiStateMother.success(listOf(cartItem))
        val currencyFormatter = NumberFormat.getCurrencyInstance().apply {
            currency = getInstance("USD")
        }
        val unitPrice = when (cartItem.item.promotion) {
            is ProductPromotion.Percent -> cartItem.item.promotion.discountedPrice
            else -> cartItem.item.product.price
        }

        //WHEN
        renderScreen(uiState)

        //THEN
        composeRule.onNodeWithTag(idResources(R.id.cart_item_card, cartItem.cartItem.productId))
            .assertIsDisplayed()
        composeRule.onNodeWithText(
            currencyFormatter.format(cartItem.item.product.price)
        ).assertIsDisplayed()
        composeRule.onNodeWithText(
            stringResources(R.string.c_u, currencyFormatter.format(unitPrice))
        ).assertIsDisplayed()
    }

    @Test
    fun should_show_item_total_price_when_cart_has_items_in_uiState() {
        //GIVEN
        val cartItems = CartUiStateMother.defaultCartItems
        val uiState = CartUiStateMother.success(cartItems)
        val currencyFormatter = NumberFormat.getCurrencyInstance().apply {
            currency = getInstance("USD")
        }
        val itemFirst = cartItems.first()
        val itemFirstTotal = itemFirst.item.product.price * itemFirst.cartItem.quantity
        val itemSecond = cartItems.last()
        val itemSecondTotal = itemSecond.item.product.price * itemSecond.cartItem.quantity

        //WHEN
        renderScreen(uiState)

        //THEN
        composeRule.onNodeWithText(
            stringResources(R.string.total_with_amount, currencyFormatter.format(itemFirstTotal))
        ).assertIsDisplayed()
        composeRule.onNodeWithText(
            stringResources(R.string.total_with_amount, currencyFormatter.format(itemSecondTotal))
        ).assertIsDisplayed()

    }

    @Test
    fun should_show_current_quantity_in_quantity_selector_when_cart_has_items_in_uiState() {
        //GIVEN
        val cartItems = CartUiStateMother.defaultCartItems
        val uiState = CartUiStateMother.success(cartItems)
        val itemFirst = cartItems.first()
        //WHEN
        renderScreen(uiState)

        //THEN
        composeRule.onNodeWithTag(idResources(R.id.cart_list_lasy)).performScrollToNode(
            hasTestTag(idResources(R.id.cart_item_card, itemFirst.cartItem.productId))
        ).assertIsDisplayed()
        composeRule.onNodeWithTag(
            idResources(
                R.id.cart_quantity_selector_item,
                itemFirst.cartItem.productId
            )
        )
            .assertIsDisplayed()
        composeRule.onNodeWithText(itemFirst.cartItem.quantity.toString()).assertIsDisplayed()
    }

    @Test
    fun should_disable_decrease_button_when_quantity_is_one_in_uiState() {
        //GIVEN
        val uiState = CartUiStateMother.successWithOneProduct(quantity = 1)
        val cartItem = uiState.cartItems.first()

        //WHEN
        renderScreen(uiState)

        //THEN
        composeRule.onNodeWithTag(idResources(R.id.cart_list_lasy)).performScrollToNode(
            hasTestTag(idResources(R.id.cart_item_card, cartItem.cartItem.productId))
        ).assertIsDisplayed()
        composeRule.onNodeWithTag(
            idResources(
                R.id.cart_quantity_selector_item,
                cartItem.cartItem.productId
            )
        )
            .assertIsDisplayed()
        composeRule.onNodeWithTag(idResources(R.id.quantity_selector_decrease, cartItem.cartItem.productId))
            .assertIsNotEnabled()

    }

    @Test
    fun should_enable_decrease_button_when_quantity_is_greater_than_one_in_uiState() {
        //GIVEN
        val uiState = CartUiStateMother.successWithOneProduct(quantity = 3)
        val cartItem = uiState.cartItems.first()

        //WHEN
        renderScreen(uiState)

        //THEN
        composeRule.onNodeWithTag(idResources(R.id.cart_list_lasy)).performScrollToNode(
            hasTestTag(idResources(R.id.cart_item_card, cartItem.cartItem.productId))
        ).assertIsDisplayed()
        composeRule.onNodeWithTag(
            idResources(
                R.id.cart_quantity_selector_item,
                cartItem.cartItem.productId
            )
        )
            .assertIsDisplayed()
        composeRule.onNodeWithTag(idResources(R.id.quantity_selector_decrease, cartItem.cartItem.productId))
            .assertIsEnabled()

    }

    @Test
    fun should_disable_increase_button_when_quantity_equals_stock_in_uiState() {
        //GIVEN
        val uiState = CartUiStateMother.successWithOneProduct(quantity = 2, stock = 2)
        val cartItem = uiState.cartItems.first()

        //WHEN
        renderScreen(uiState)

        //THEN
        composeRule.onNodeWithTag(idResources(R.id.cart_list_lasy)).performScrollToNode(
            hasTestTag(idResources(R.id.cart_item_card, cartItem.cartItem.productId))
        ).assertIsDisplayed()
        composeRule.onNodeWithTag(
            idResources(
                R.id.cart_quantity_selector_item,
                cartItem.cartItem.productId
            )
        )
            .assertIsDisplayed()
        composeRule.onNodeWithTag(idResources(R.id.quantity_selector_increase, cartItem.cartItem.productId))
            .assertIsNotEnabled()

    }

    @Test
    fun should_enable_increase_button_when_quantity_is_less_than_stock_in_uiState() {
        //GIVEN
        val uiState = CartUiStateMother.successWithOneProduct(quantity = 1, stock = 2)
        val cartItem = uiState.cartItems.first()

        //WHEN
        renderScreen(uiState)

        //THEN
        composeRule.onNodeWithTag(idResources(R.id.cart_list_lasy)).performScrollToNode(
            hasTestTag(idResources(R.id.cart_item_card, cartItem.cartItem.productId))
        ).assertIsDisplayed()
        composeRule.onNodeWithTag(
            idResources(
                R.id.cart_quantity_selector_item,
                cartItem.cartItem.productId
            )
        )
            .assertIsDisplayed()
        composeRule.onNodeWithTag(idResources(R.id.quantity_selector_increase, cartItem.cartItem.productId))
            .assertIsEnabled()

    }

    // ─── Success — resumen ────────────────────────────────────────────────────────

    @Test
    fun should_show_summary_card_when_cart_has_items_and_summary_is_not_null_in_uiState() {
        //GIVEN
        val uiState = CartUiStateMother.successWithOneProduct(quantity = 1, stock = 2)
        val cartItem = uiState.cartItems.first()

        //WHEN
        renderScreen(uiState)

        //THEN
        composeRule.onNodeWithTag(idResources(R.id.cart_list_lasy)).performScrollToNode(
            hasTestTag(idResources(R.id.cart_item_card, cartItem.cartItem.productId))
        ).assertIsDisplayed()
        composeRule.onNodeWithTag(
            idResources(
                R.id.cart_quantity_selector_item,
                cartItem.cartItem.productId
            )
        ).assertIsDisplayed()
        composeRule.onNodeWithTag(idResources(R.id.cart_summary)).assertIsDisplayed()

    }

    @Test
    fun should_show_subtotal_in_summary_when_cart_has_items_in_uiState() {
        //GIVEN
        val uiState = CartUiStateMother.success()
        val currencyFormatter = NumberFormat.getCurrencyInstance().apply {
            currency = getInstance("USD")
        }
        //WHEN
        renderScreen(uiState)

        //THEN
        composeRule.onNodeWithTag(idResources(R.id.cart_summary))
            .assertIsDisplayed()
        composeRule.onNodeWithText(stringResources(R.string.sub_total)).assertIsDisplayed()
        composeRule.onAllNodesWithText(currencyFormatter.format(uiState.summary!!.subtotal))
            .onFirst().assertIsDisplayed()

    }

    @Test
    fun should_show_total_in_summary_when_cart_has_items_in_uiState() {
        //GIVEN
        val uiState = CartUiStateMother.success(
            cartItems = CartUiStateMother.defaultCartItems,
            summary = CartUiStateMother.defaultSummary
        )
        val currencyFormatter = NumberFormat.getCurrencyInstance().apply {
            currency = getInstance("USD")
        }
        //WHEN
        renderScreen(uiState)

        //THEN
        composeRule.onNodeWithText(stringResources(R.string.total)).assertIsDisplayed()
        composeRule.onAllNodesWithText(currencyFormatter.format(uiState.summary!!.finalTotal))
            .onFirst().assertIsDisplayed()

    }

    @Test
    fun should_show_discount_row_in_summary_when_discount_total_is_greater_than_zero_in_uiState() {
        //GIVEN
        val uiState =
            CartUiStateMother.success(summary = CartUiStateMother.defaultSummaryWithDiscount)
        val currencyFormatter = NumberFormat.getCurrencyInstance().apply {
            currency = getInstance("USD")
        }
        //WHEN
        renderScreen(uiState)

        //THEN
        composeRule.onNodeWithTag(idResources(R.id.cart_discount_row)).assertIsDisplayed()
        composeRule.onNodeWithText(stringResources(R.string.discount)).assertIsDisplayed()
        composeRule.onNodeWithText(currencyFormatter.format(uiState.summary!!.discountTotal))
            .assertIsDisplayed()

    }

    @Test
    fun should_not_show_discount_row_in_summary_when_discount_total_is_zero_in_uiState() {
        //GIVEN
        val uiState = CartUiStateMother.success(summary = CartUiStateMother.defaultSummary)

        //WHEN
        renderScreen(uiState)

        //THEN
        composeRule.onNodeWithTag(idResources(R.id.cart_discount_row)).assertDoesNotExist()


    }

    @Test
    fun should_not_show_summary_card_when_summary_is_null_in_uiState() {
        //GIVEN
        val uiState = CartUiStateMother.success(summary = null)
        //WHEN
        renderScreen(uiState)

        //THEN
        composeRule.onNodeWithTag(idResources(R.id.cart_summary))
            .assertDoesNotExist()
    }

    // ─── Callbacks ───────────────────────────────────────────────────────────────

    @Test
    fun should_call_onBack_when_back_button_is_clicked_in_onBack() {
        //GIVEN
        val uiState = CartUiStateMother.success(summary = null)
        var isClicked: Boolean? = null
        renderScreen(uiState, onBack = { isClicked = true })

        //WHEN
        composeRule.onNodeWithTag(idResources(R.id.top_bar_back_navigation_icon))
            .performClick()

        //THEN
        assertEquals(true, isClicked)
    }

    @Test
    fun should_call_onIncreaseQuantity_with_product_id_and_quantity_when_increase_button_is_clicked_in_onIncreaseQuantity() {
        //GIVEN
        val uiState = CartUiStateMother.successWithOneProduct(1, 3)
        val cartItem = uiState.cartItems.first()
        var data: Pair<String, Int>? = null
        renderScreen(uiState, onIncreaseQuantity = { id, q ->
            data = Pair(id, q)
        })

        //WHEN
        composeRule.onNodeWithTag(idResources(R.id.cart_list_lasy)).performScrollToNode(
            hasTestTag(idResources(R.id.cart_item_card, cartItem.cartItem.productId))
        ).assertIsDisplayed()

        composeRule.onNodeWithTag(
            idResources(R.id.quantity_selector_increase, cartItem.cartItem.productId)
        ).performClick()

        //THEN
        assertEquals(cartItem.cartItem.productId, data?.first)
        assertEquals(cartItem.cartItem.quantity, data?.second)
    }

    @Test
    fun should_call_onDecreaseQuantity_with_product_id_and_quantity_when_decrease_button_is_clicked_in_onDecreaseQuantity() {
        //GIVEN
        val uiState = CartUiStateMother.successWithOneProduct(2, 3)
        val cartItem = uiState.cartItems.first()
        var data: Pair<String, Int>? = null
        renderScreen(uiState, onDecreaseQuantity = { id, q ->
            data = Pair(id, q)
        })
        //WHEN
        composeRule.onNodeWithTag(
            idResources(R.id.cart_list_lasy)
        ).performScrollToNode(
            hasTestTag(idResources(R.id.cart_item_card, cartItem.cartItem.productId))
        ).assertIsDisplayed()


        composeRule.onNodeWithTag(
            idResources(R.id.quantity_selector_decrease, cartItem.cartItem.productId))
            .performClick()

        //THEN
        assertEquals(cartItem.cartItem.productId, data?.first)
        assertEquals(cartItem.cartItem.quantity, data?.second)
    }

    @SuppressLint("CheckResult")
    @Test
    fun should_call_onRemove_with_product_id_when_item_is_swiped_in_onRemove() {
        //GIVEN
        val uiState = CartUiStateMother.successWithOneProduct(1, 3)
        val cartItem = uiState.cartItems.first()
        var id: String? = null
        renderScreen(uiState, onRemove = {
            id = it
        })

        //WHEN
        composeRule.onNodeWithTag(idResources(R.id.cart_list_lasy)).performScrollToNode(
            hasTestTag(
                idResources(R.id.cart_item_card, cartItem.cartItem.productId)
            )
        ).assertIsDisplayed()

        composeRule.onNodeWithTag(
            idResources(R.id.cart_item_card, cartItem.cartItem.productId)
        ).performTouchInput { swipeRight() }

        //THEN
        assertEquals(cartItem.cartItem.productId, id)
    }
}
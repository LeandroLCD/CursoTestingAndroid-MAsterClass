package com.aristidevs.cursotestingandroid.productlist.presentation


import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertIsNotSelected
import androidx.compose.ui.test.assertIsSelected
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollToIndex
import com.aristidevs.cursotestingandroid.R
import com.aristidevs.cursotestingandroid.core.BaseComposeTest
import com.aristidevs.cursotestingandroid.core.uiState.ProductListUiStateMother
import com.aristidevs.cursotestingandroid.productlist.domain.model.SortOption
import org.junit.Assert.assertEquals
import org.junit.Test

class ProductListScreenTest : BaseComposeTest() {

    fun renderProductListContent(
        uiState: ProductListUiState,
        filterVisible: Boolean = false,
        cartItemCount: Int = 0,
        navigateToSettings: () -> Unit = {},
        navigateToProductDetail: (String) -> Unit = {},
        navigateToCart: () -> Unit = {},
        setFilterVisible: (Boolean) -> Unit = {},
        onCategorySelected: (String?) -> Unit = {},
        onSortSelected: (SortOption) -> Unit = {}
    ) {
        composeRule.setContent {
            ProductListScreenContent(
                uiState = uiState,
                snackbarHostState = rememberSnackbarHostState(),
                filterVisible = filterVisible,
                cartItemCount = cartItemCount,
                navigateToSettings = navigateToSettings,
                navigateToProductDetail = navigateToProductDetail,
                navigateToCart = navigateToCart,
                setFilterVisible = setFilterVisible,
                onCategorySelected = onCategorySelected,
                onSortSelected = onSortSelected
            )
        }

    }


    @Test
    fun should_show_loading_on_ui_loading_state() {
        // GIVEN
        val uiState = ProductListUiState.Loading

        //WHEN
        renderProductListContent(
            uiState = uiState
        )

        //THEN
        composeRule.onNodeWithTag(idResources(R.id.loading_progress_indicator)).assertIsDisplayed()

    }

    @Test
    fun should_show_error_on_ui_error_state() {
        //GIVEN
        val uiState = ProductListUiState.Error("Error message")

        //WHEN
        renderProductListContent(
            uiState = uiState
        )

        //THEN
        composeRule.onNodeWithText(stringResources(R.string.error)).assertIsDisplayed()
        composeRule.onNodeWithText(uiState.message).assertIsDisplayed()
    }

    @Test
    fun should_show_productsAndCount_on_ui_success_state() {
        //GIVEN
        val uiState = ProductListUiStateMother.success()
        val firstProduct = uiState.products.first()
        val index = uiState.products.lastIndex
        val lastProduct = uiState.products[index]

        //WHEN
        renderProductListContent(
            uiState = uiState
        )
        //THEN
        composeRule.onNodeWithText(stringResources(R.string.products, uiState.products.size)).assertIsDisplayed()

        composeRule.onNodeWithTag(idResources(R.id.product_item, firstProduct.product.id)).assertIsDisplayed()
        /*Busqueda exacta*/
        composeRule.onNodeWithTag(idResources(R.id.product_list_lazy)).performScrollToIndex(index)
        /*busqueda activa*/
        /*composeRule.onNodeWithTag(idResources(R.id.product_list_lazy)).performScrollToNode(
            hasTestTag(idResources(R.id.product_item, lastProduct.product.id))
        )*/
        composeRule.onNodeWithTag(idResources(R.id.product_item, lastProduct.product.id)).assertIsDisplayed()
    }
    @Test
    fun should_show_menu_on_ui_success_state() {
        //GIVEN
        val uiState = ProductListUiStateMother.success()
        //WHEN
        renderProductListContent(
            uiState = uiState,
            filterVisible = true
        )
        //THEN
        composeRule.onNodeWithTag(idResources(R.id.filter_menu)).assertIsDisplayed()
    }
    @Test
    fun should_empty_products_on_ui_success_state() {
        //GIVEN
        val uiState = ProductListUiStateMother.success(products = emptyList())
        //WHEN
        renderProductListContent(
            uiState = uiState
        )
        //THEN
        composeRule.onNodeWithText(stringResources(R.string.products_not_found)).assertIsDisplayed()
    }
    @Test
    fun should_show_all_categories_on_ui_success_state() {
        //GIVEN
        val uiState = ProductListUiStateMother.success(selectedCategory = null)
        //WHEN
        renderProductListContent(
            uiState = uiState, filterVisible = true
        )
        //THEN
        composeRule.onNodeWithTag(idResources(R.id.category)).assertIsDisplayed()
        composeRule.onNodeWithTag(idResources(R.id.category)).assertIsSelected()
        uiState.categories.forEach { category ->
            composeRule.onNodeWithTag(idResources(R.id.category, category)).assertIsDisplayed()
        }

    }
    @Test
    fun should_show_selected_category_on_ui_success_state() {
        //GIVEN
        val category = ProductListUiStateMother.defaultProducts.first().product.category
        val uiState = ProductListUiStateMother.success(selectedCategory = category)
        //WHEN
        renderProductListContent(
            uiState = uiState, filterVisible = true
        )
        //THEN
        composeRule.onNodeWithTag(idResources(R.id.category, category)).assertIsSelected()
    }

   
    @Test
    fun should_show_price_asc_chip_selected_when_sort_option_is_price_asc_in_sortOption() {
        //GIVEN
        val uiState = ProductListUiStateMother.success(sortOption = SortOption.PRICE_ASC)

        //WHEN
        renderProductListContent(uiState = uiState, filterVisible = true)

        //THEN
        composeRule.onNodeWithText(stringResources(R.string.sort_price_asc)).assertIsSelected()
        composeRule.onNodeWithText(stringResources(R.string.sort_price_desc)).assertIsNotSelected()
        composeRule.onNodeWithText(stringResources(R.string.sort_discount)).assertIsNotSelected()
    }

    @Test
    fun should_show_price_desc_chip_selected_when_sort_option_is_price_desc_in_sortOption() {
        //GIVEN
        val uiState = ProductListUiStateMother.success(sortOption = SortOption.PRICE_DESC)

        //WHEN
        renderProductListContent(uiState = uiState, filterVisible = true)

        //THEN
        composeRule.onNodeWithText(stringResources(R.string.sort_price_desc)).assertIsSelected()
        composeRule.onNodeWithText(stringResources(R.string.sort_price_asc)).assertIsNotSelected()
        composeRule.onNodeWithText(stringResources(R.string.sort_discount)).assertIsNotSelected()
    }

    @Test
    fun should_show_discount_chip_selected_when_sort_option_is_discount_in_sortOption() {
        //GIVEN
        val uiState = ProductListUiStateMother.success(sortOption = SortOption.DISCOUNT)

        //WHEN
        renderProductListContent(uiState = uiState, filterVisible = true)

        //THEN
        composeRule.onNodeWithText(stringResources(R.string.sort_discount)).assertIsSelected()
        composeRule.onNodeWithText(stringResources(R.string.sort_price_asc)).assertIsNotSelected()
        composeRule.onNodeWithText(stringResources(R.string.sort_price_desc)).assertIsNotSelected()
    }

    @Test
    fun should_show_no_sort_chip_selected_when_sort_option_is_none_in_sortOption() {
        //GIVEN
        val uiState = ProductListUiStateMother.success(sortOption = SortOption.NONE)

        //WHEN
        renderProductListContent(uiState = uiState, filterVisible = true)

        //THEN
        composeRule.onNodeWithText(stringResources(R.string.sort_price_asc)).assertIsNotSelected()
        composeRule.onNodeWithText(stringResources(R.string.sort_price_desc)).assertIsNotSelected()
        composeRule.onNodeWithText(stringResources(R.string.sort_discount)).assertIsNotSelected()
    }

    @Test
    fun should_call_onSortSelected_with_price_asc_when_price_asc_chip_is_clicked_in_onSortSelected() {
        //GIVEN
        val uiState = ProductListUiStateMother.success(sortOption = SortOption.NONE)
        var sortSelected: SortOption? = null

        //WHEN
        renderProductListContent(
            uiState = uiState,
            filterVisible = true,
            onSortSelected = { sortSelected = it }
        )
        composeRule.onNodeWithText(stringResources(R.string.sort_price_asc)).performClick()

        //THEN
        assertEquals(SortOption.PRICE_ASC, sortSelected)
    }

    @Test
    fun should_call_onSortSelected_with_price_desc_when_price_desc_chip_is_clicked_in_onSortSelected() {
        //GIVEN
        val uiState = ProductListUiStateMother.success(sortOption = SortOption.NONE)
        var sortSelected: SortOption? = null

        //WHEN
        renderProductListContent(
            uiState = uiState,
            filterVisible = true,
            onSortSelected = { sortSelected = it }
        )
        composeRule.onNodeWithText(stringResources(R.string.sort_price_desc)).performClick()

        //THEN
        assertEquals(SortOption.PRICE_DESC, sortSelected)
    }

    @Test
    fun should_call_onSortSelected_with_discount_when_discount_chip_is_clicked_in_onSortSelected() {
        //GIVEN
        val uiState = ProductListUiStateMother.success(sortOption = SortOption.NONE)
        var sortSelected: SortOption? = null

        //WHEN
        renderProductListContent(
            uiState = uiState,
            filterVisible = true,
            onSortSelected = { sortSelected = it }
        )
        composeRule.onNodeWithText(stringResources(R.string.sort_discount)).performClick()

        //THEN
        assertEquals(SortOption.DISCOUNT, sortSelected)
    }


    @Test
    fun should_not_show_badge_when_cart_item_count_is_zero_in_cartItemCount() {
        //GIVEN
        val uiState = ProductListUiStateMother.success()

        //WHEN
        renderProductListContent(uiState = uiState, cartItemCount = 0)

        //THEN
        composeRule.onNodeWithTag(idResources(R.id.top_app_bar_badge)).assertDoesNotExist()
    }

    @Test
    fun should_show_badge_with_count_when_cart_item_count_is_50_in_cartItemCount() {
        //GIVEN
        val uiState = ProductListUiStateMother.success()
        val cartItemCount = 50

        //WHEN
        renderProductListContent(uiState = uiState, cartItemCount = cartItemCount)

        //THEN
        composeRule.onNodeWithTag(idResources(R.id.top_app_bar_badge)).assertIsDisplayed()
        composeRule.onNodeWithText(cartItemCount.toString()).assertIsDisplayed()
    }

    @Test
    fun should_show_badge_with_max_text_when_cart_item_count_is_100_in_cartItemCount() {
        //GIVEN
        val uiState = ProductListUiStateMother.success()

        //WHEN
        renderProductListContent(uiState = uiState, cartItemCount = 100)

        //THEN
        composeRule.onNodeWithTag(idResources(R.id.top_app_bar_badge)).assertIsDisplayed()
        composeRule.onNodeWithText(stringResources(R.string.cart_iten_count_max)).assertIsDisplayed()
    }


    @Test
    fun should_call_navigateToSettings_when_settings_button_is_clicked_in_navigateToSettings() {
        //GIVEN
        val uiState = ProductListUiStateMother.success()
        var navigateCalled = false

        //WHEN
        renderProductListContent(
            uiState = uiState,
            navigateToSettings = { navigateCalled = true }
        )
        composeRule.onNodeWithTag(idResources(R.id.top_app_bar_settings_button)).performClick()

        //THEN
        assertEquals(true, navigateCalled)
    }

    @Test
    fun should_call_navigateToCart_when_cart_button_is_clicked_in_navigateToCart() {
        //GIVEN
        val uiState = ProductListUiStateMother.success()
        var navigateCalled = false

        //WHEN
        renderProductListContent(
            uiState = uiState,
            navigateToCart = { navigateCalled = true }
        )
        composeRule.onNodeWithTag(idResources(R.id.top_app_bar_cart_button)).performClick()

        //THEN
        assertEquals(true, navigateCalled)
    }

    @Test
    fun should_call_setFilterVisible_with_true_when_filter_button_is_clicked_and_filters_are_hidden_in_setFilterVisible() {
        //GIVEN
        val uiState = ProductListUiStateMother.success()
        var filterVisibleValue: Boolean? = null

        //WHEN
        renderProductListContent(
            uiState = uiState,
            filterVisible = false,
            setFilterVisible = { filterVisibleValue = it }
        )
        composeRule.onNodeWithTag(idResources(R.id.top_app_bar_filter_button)).performClick()

        //THEN
        assertEquals(true, filterVisibleValue)
    }

    @Test
    fun should_call_setFilterVisible_with_false_when_filter_button_is_clicked_and_filters_are_visible_in_setFilterVisible() {
        //GIVEN
        val uiState = ProductListUiStateMother.success()
        var filterVisibleValue: Boolean? = null

        //WHEN
        renderProductListContent(
            uiState = uiState,
            filterVisible = true,
            setFilterVisible = { filterVisibleValue = it }
        )
        composeRule.onNodeWithTag(idResources(R.id.top_app_bar_filter_button)).performClick()

        //THEN
        assertEquals(false, filterVisibleValue)
    }

    @Test
    fun should_call_navigateToProductDetail_with_product_id_when_product_item_is_clicked_in_navigateToProductDetail() {
        //GIVEN
        val uiState = ProductListUiStateMother.success()
        val expectedProduct = uiState.products.first()
        var navigatedProductId: String? = null

        //WHEN
        renderProductListContent(
            uiState = uiState,
            navigateToProductDetail = { navigatedProductId = it }
        )
        composeRule.onNodeWithTag(idResources(R.id.product_item, expectedProduct.product.id)).performClick()

        //THEN
        assertEquals(expectedProduct.product.id, navigatedProductId)
    }

    @Test
    fun should_call_onCategorySelected_with_category_when_category_chip_is_clicked_in_onCategorySelected() {
        //GIVEN
        val uiState = ProductListUiStateMother.success()
        val expectedCategory = uiState.categories.first()
        var selectedCategory: String? = null

        //WHEN
        renderProductListContent(
            uiState = uiState,
            filterVisible = true,
            onCategorySelected = { selectedCategory = it }
        )
        composeRule.onNodeWithTag(idResources(R.id.category, expectedCategory)).performClick()

        //THEN
        assertEquals(expectedCategory, selectedCategory)
    }

    @Test
    fun should_call_onSortSelected_with_sort_option_when_sort_chip_is_clicked_in_onSortSelected() {
        //GIVEN
        val uiState = ProductListUiStateMother.success(sortOption = SortOption.NONE)
        var selectedSort: SortOption? = null

        //WHEN
        renderProductListContent(
            uiState = uiState,
            filterVisible = true,
            onSortSelected = { selectedSort = it }
        )
        composeRule.onNodeWithText(stringResources(R.string.sort_price_asc)).performClick()

        //THEN
        assertEquals(SortOption.PRICE_ASC, selectedSort)
    }

}


package com.aristidevs.cursotestingandroid.productlist.presentation

import androidx.lifecycle.viewModelScope
import androidx.test.ext.junit.runners.AndroidJUnit4
import app.cash.turbine.test
import com.aristidevs.cursotestingandroid.cart.domain.repository.CartItemRepository
import com.aristidevs.cursotestingandroid.cart.domain.usecase.GetCartItemCountUseCase
import com.aristidevs.cursotestingandroid.core.MainDispatcherRule
import com.aristidevs.cursotestingandroid.core.mockwebserver.MiniMarketApiOkhttpDispatcher
import com.aristidevs.cursotestingandroid.core.mockwebserver.rules.MockWebServerRule
import com.aristidevs.cursotestingandroid.core.utils.TestAssets.asAssets
import com.aristidevs.cursotestingandroid.core.utils.awaitMatches
import com.aristidevs.cursotestingandroid.productlist.domain.model.SortOption
import com.aristidevs.cursotestingandroid.productlist.domain.repository.ProductRepository
import com.aristidevs.cursotestingandroid.productlist.domain.repository.PromotionRepository
import com.aristidevs.cursotestingandroid.productlist.domain.repository.SettingsRepository
import com.aristidevs.cursotestingandroid.productlist.domain.usecase.GetProductsUseCase
import dagger.hilt.android.testing.HiltAndroidRule
import dagger.hilt.android.testing.HiltAndroidTest
import kotlinx.coroutines.cancel
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import javax.inject.Inject

@HiltAndroidTest
@RunWith(AndroidJUnit4::class)
class ProductListViewModelTest {

    @get:Rule(order = 0)
    val mockWebServer = MockWebServerRule()

    @get:Rule(order = 1)
    val hiltRule = HiltAndroidRule(this)

    @get:Rule(order = 2)
    val mainDispatcherRule = MainDispatcherRule()

    @Inject
    lateinit var getProductsUseCase: GetProductsUseCase

    @Inject
    lateinit var getCartItemCountUseCase: GetCartItemCountUseCase

    @Inject
    lateinit var settingsRepository: SettingsRepository

    @Inject
    lateinit var cartItemRepository: CartItemRepository

    @Inject
    lateinit var productRepository: ProductRepository

    @Inject
    lateinit var promotionRepository: PromotionRepository



    @Before
    fun setUp() = runTest{
        mockWebServer.mockWebServer.dispatcher = MiniMarketApiOkhttpDispatcher(
            "product_list_default.json".asAssets()
        )
        hiltRule.inject()
        productRepository.refreshProduct()
        promotionRepository.refreshPromotions()

    }
    
    private lateinit var viewModel: ProductListViewModel
    
    @After
    fun tearDown() {
        if (::viewModel.isInitialized) {
            viewModel.viewModelScope.cancel()
        }
    }


    private fun createViewModel() {
        viewModel = ProductListViewModel(
            getProductsUseCase,
            getCartItemCountUseCase,
            settingsRepository
        )
    }

    // ─── uiState ─────────────────────────────────────────────────────────────────

    @Test
    fun should_emit_loading_uiState_when_viewModel_is_created_in_uiState() = runTest {

        //WHEN
         createViewModel()

        //THEN
        viewModel.uiState.test {
            val state = awaitItem()
            assertTrue(state is ProductListUiState.Loading)
            cancelAndConsumeRemainingEvents()
        }
    }

    @Test
    fun should_emit_success_uiState_with_products_when_api_response_is_successful_in_uiState() = runTest {
        //GIVEN
       createViewModel()

        //WHEN & THEN
        viewModel.uiState.test {
            val state = awaitMatches { it is ProductListUiState.Success }
            val success = state as ProductListUiState.Success
            assertEquals(3, success.products.size)
            cancelAndConsumeRemainingEvents()
        }
    }

    @Test
    fun should_emit_success_uiState_with_categories_when_products_have_multiple_categories_in_uiState() = runTest {
        //GIVEN
        createViewModel()

        //WHEN & THEN
        viewModel.uiState.test {
            val state = awaitMatches { it is ProductListUiState.Success }
            val success = state as ProductListUiState.Success
            assertEquals(listOf("fruits", "vegetables"), success.categories)
            cancelAndConsumeRemainingEvents()
        }
    }

    // ─── setCategory ────────────────────────────────────────────────────────────

    @Test
    fun should_filter_products_by_category_when_category_is_set_in_setCategory() = runTest {
        //GIVEN
        createViewModel()

        viewModel.uiState.test {
            awaitMatches { it is ProductListUiState.Success }

            //WHEN
            viewModel.setCategory("fruits")

            //THEN
            val state = awaitMatches { (it as? ProductListUiState.Success)?.selectedCategory == "fruits" }
            val success = state as ProductListUiState.Success
            assertEquals(2, success.products.size)
            assertTrue(success.products.all { it.product.category == "fruits" })
            cancelAndConsumeRemainingEvents()
        }
    }

    @Test
    fun should_show_all_products_when_category_is_cleared_in_setCategory() = runTest {
        //GIVEN
        createViewModel()

        viewModel.uiState.test {
            awaitMatches { it is ProductListUiState.Success }
            viewModel.setCategory("fruits")
            awaitMatches { (it as? ProductListUiState.Success)?.selectedCategory == "fruits" }

            //WHEN
            viewModel.setCategory(null)

            //THEN
            val state = awaitMatches { (it as? ProductListUiState.Success)?.selectedCategory == null }
            val success = state as ProductListUiState.Success
            assertEquals(3, success.products.size)
            cancelAndConsumeRemainingEvents()
        }
    }

    // ─── setSortOption ───────────────────────────────────────────────────────────

    @Test
    fun should_sort_products_by_price_ascending_when_sort_option_is_price_asc_in_setSortOption() = runTest {
        //GIVEN
        createViewModel()

        viewModel.uiState.test {
            awaitMatches { it is ProductListUiState.Success }

            //WHEN
            viewModel.setSortOption(SortOption.PRICE_ASC)

            //THEN
            val state = awaitMatches { (it as? ProductListUiState.Success)?.sortOption == SortOption.PRICE_ASC }
            val success = state as ProductListUiState.Success
            val prices = success.products.map { it.product.price }
            assertEquals(listOf(0.50, 1.00, 2.00), prices)
            cancelAndConsumeRemainingEvents()
        }
    }

    @Test
    fun should_sort_products_by_price_descending_when_sort_option_is_price_desc_in_setSortOption() = runTest {
        //GIVEN
        createViewModel()

        viewModel.uiState.test {
            awaitMatches { it is ProductListUiState.Success }

            //WHEN
            viewModel.setSortOption(SortOption.PRICE_DESC)

            //THEN
            val state = awaitMatches { (it as? ProductListUiState.Success)?.sortOption == SortOption.PRICE_DESC }
            val success = state as ProductListUiState.Success
            val prices = success.products.map { it.product.price }
            assertEquals(listOf(2.00, 1.00, 0.50), prices)
            cancelAndConsumeRemainingEvents()
        }
    }

    @Test
    fun should_sort_products_by_discount_first_when_sort_option_is_discount_in_setSortOption() = runTest {
        //GIVEN revisar
        createViewModel()

        viewModel.uiState.test {
            awaitMatches { it is ProductListUiState.Success }

            //WHEN
            viewModel.setSortOption(SortOption.DISCOUNT)

            //THEN
            val state = awaitMatches { (it as? ProductListUiState.Success)?.sortOption == SortOption.DISCOUNT }
            val success = state as ProductListUiState.Success
            assertEquals(success.products.size, 3)
            cancelAndConsumeRemainingEvents()
        }
    }

    // ─── setFilterVisible ─────────────────────────────────────────────────────

    @Test
    fun should_update_filter_visible_to_false_when_set_filter_visible_is_called_with_false_in_setFilterVisible() = runTest {
        //GIVEN
        createViewModel()

        viewModel.filterVisible.test {

            //WHEN
            viewModel.setFilterVisible(false)
            val isVisible = awaitMatches { !it }

            //THEN
            assertFalse(isVisible)
            cancelAndConsumeRemainingEvents()
        }
    }

    @Test
    fun should_update_filter_visible_to_true_when_set_filter_visible_is_called_with_true_in_setFilterVisible() = runTest {
        //GIVEN

        createViewModel()

        viewModel.filterVisible.test {

            //WHEN
            viewModel.setFilterVisible(true)

            //THEN
            val visible = awaitItem()
            assertEquals(true, visible)
            cancelAndConsumeRemainingEvents()
        }
    }

    // ─── cartItemCount ────────────────────────────────────────────────────────

    @Test
    fun should_emit_zero_cart_item_count_when_cart_is_empty_in_cartItemCount() = runTest {
        //GIVEN
        createViewModel()

        //WHEN & THEN
        viewModel.cartItemCount.test {
            val count = awaitItem()
            assertEquals(0, count)
            cancelAndConsumeRemainingEvents()
        }
    }

    @Test
    fun should_emit_cart_item_count_when_items_are_added_to_cart_in_cartItemCount() = runTest {
        //GIVEN
        createViewModel()

        viewModel.cartItemCount.test {

            //WHEN
            cartItemRepository.addToCart("1", 3)

            //THEN
            val count = awaitMatches { it > 0 }
            assertEquals(3, count)
            cancelAndConsumeRemainingEvents()
        }
    }
}
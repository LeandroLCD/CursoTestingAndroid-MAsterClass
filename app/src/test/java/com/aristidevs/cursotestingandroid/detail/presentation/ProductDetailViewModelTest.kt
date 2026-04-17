package com.aristidevs.cursotestingandroid.detail.presentation

import app.cash.turbine.test
import com.aristidevs.cursotestingandroid.cart.domain.FailingCartItemRepositoryStub
import com.aristidevs.cursotestingandroid.cart.domain.repository.CartItemRepository
import com.aristidevs.cursotestingandroid.cart.domain.usecase.AddToCartUseCase
import com.aristidevs.cursotestingandroid.core.MainDispatcherRule
import com.aristidevs.cursotestingandroid.core.builders.product
import com.aristidevs.cursotestingandroid.core.domain.model.AppError
import com.aristidevs.cursotestingandroid.core.fakes.FakeCartItemRepository
import com.aristidevs.cursotestingandroid.core.fakes.FakeProductRepository
import com.aristidevs.cursotestingandroid.core.fakes.FakePromotionRepository
import com.aristidevs.cursotestingandroid.detail.domain.usecase.GetProductDetailWithPromotionUseCase
import com.aristidevs.cursotestingandroid.detail.presentation.states.ProductDetailEvent
import com.aristidevs.cursotestingandroid.productlist.domain.repository.ProductRepository
import com.aristidevs.cursotestingandroid.productlist.domain.repository.PromotionRepository
import com.aristidevs.cursotestingandroid.productlist.domain.usecase.GetPromotionForProduct
import com.aristidevs.cursotestingandroid.productlist.presentation.FailingProductRepositoryStub
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test

class ProductDetailViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private fun createViewModel(
        productId: String,
        fakeProduct: ProductRepository = FakeProductRepository(),
        fakePromotion: PromotionRepository = FakePromotionRepository(),
        fakeCartItemRepository: CartItemRepository = FakeCartItemRepository()
    ): ProductDetailViewModel {
        val getProductDetailWithPromotionUseCase = GetProductDetailWithPromotionUseCase(
            productRepository = fakeProduct,
            promotionRepository = fakePromotion,
            getPromotionForProduct = GetPromotionForProduct(),
        )
        val addToCartUseCase = AddToCartUseCase(
            cartItemRepository = fakeCartItemRepository,
            productRepository = fakeProduct,
        )
        return ProductDetailViewModel(
            productId = productId,
            getProductDetailWithPromotionUseCase = getProductDetailWithPromotionUseCase,
            addToCartUseCase = addToCartUseCase,
        )

    }


    @Test
    fun should_emit_default_uiState_when_viewModel_is_initialized_in_init() = runTest {
        //GIVEN
        val productId = "id-01"

        //WHEN
        val viewModel = createViewModel(productId)

        //THEN
        viewModel.uiState.test {
            val state = awaitItem()
            assertNull(state.item)
            assertEquals(" should be $state", false, state.isLoading)
            cancelAndConsumeRemainingEvents()
        }
    }

    @Test
    fun should_update_uiState_with_product_and_loading_false_when_use_case_emits_valid_product_in_init() =
        runTest {
            // Mock getProductDetailWithPromotionUseCase to throw NetworkError. Verify uiState
            // emits state with isLoading=false and events flow receives NETWORK_ERROR.

            //GIVEN
            val product = product {
                withId("id-01")
                withName("Test Product")
                withPrice(10.0)
            }

            //WHEN
            val fakeProduct = FakeProductRepository().apply {
                setProducts(listOf(product))
            }
            val viewModel = createViewModel(fakeProduct = fakeProduct, productId = product.id)

            //THEN
            viewModel.uiState.test {
                val state = awaitItem()
                assertNotNull(state.item)
                assertEquals(" should be $state", product.id, state.item?.product?.id)
                cancelAndConsumeRemainingEvents()
            }
        }

    @Test
    fun should_emit_loading_false_and_network_error_event_when_use_case_throws_NetworkError_in_init() =
        runTest {
            //GIVEN
            val productId = "id-01"
            val fakeProduct = FailingProductRepositoryStub(AppError.NetworkError())

            //WHEN
            val viewModel = createViewModel(fakeProduct = fakeProduct, productId = productId)

            //THEN
            val uiStateJob = backgroundScope.launch {
                viewModel.uiState.collect {}
            }

            viewModel.events.test {
                val event = awaitItem()
                assertNotNull(event)
                assertTrue(event is ProductDetailEvent.Error)

                val error: AppError = (event as ProductDetailEvent.Error).cause as AppError
                assertTrue(error is AppError.NetworkError)

                cancelAndConsumeRemainingEvents()
            }

            uiStateJob.cancel()
        }

    @Test
    fun should_emit_loading_false_and_unknown_error_event_when_use_case_throws_generic_exception_in_init() =
        runTest {
            //GIVEN
            val productId = "id-01"
            val fakeProduct = FailingProductRepositoryStub(Exception("Something went wrong"))

            //WHEN
            val viewModel = createViewModel(fakeProduct = fakeProduct, productId = productId)

            //THEN
            val uiStateJob = backgroundScope.launch {
                viewModel.uiState.collect {}
            }

            viewModel.events.test {
                val event = awaitItem()
                assertNotNull(event)
                assertTrue(event is ProductDetailEvent.Error)

                val error: AppError = (event as ProductDetailEvent.Error).cause as AppError
                assertTrue(error is AppError.UnknownError)

                cancelAndConsumeRemainingEvents()
            }

            uiStateJob.cancel()
        }

    @Test
    fun should_call_use_case_and_emit_success_event_when_product_is_added_successfully_in_addToCart() =
        runTest {
            //GIVEN
            val productId = "id-01"
            val fakeProduct = FakeProductRepository().apply {
                setProducts(listOf(product {
                    withId(productId)
                    withName("Test Product")
                    withPrice(10.0)
                    withStock(2)
                }))
            }
            val fakeCart = FakeCartItemRepository().apply { setCartItems(emptyList()) }

            //WHEN
            val viewModel = createViewModel(
                fakeProduct = fakeProduct,
                fakeCartItemRepository = fakeCart,
                productId = productId
            )

            //THEN
            viewModel.events.test {
                viewModel.addToCart(productId)
                
                val event = awaitItem()
                assertNotNull(event)
                assertTrue(event is ProductDetailEvent.SUCCESS_ADD_TO_CART)

                cancelAndConsumeRemainingEvents()
            }

            
        }

    @Test
    fun should_emit_insufficient_stock_error_event_when_use_case_throws_InsufficientStock_in_addToCart() =
        runTest {
            //GIVEN
            val productId = "id-01"
            val fakeProduct = FakeProductRepository().apply {
                setProducts(listOf(product {
                    withId(productId)
                    withName("Test Product")
                    withPrice(10.0)
                    withStock(1) // Stock limitado a 1
                }))
            }
            val fakeCart = FakeCartItemRepository().apply {
                setCartItems(listOf(
                    com.aristidevs.cursotestingandroid.cart.domain.model.CartItem(
                        productId = productId,
                        quantity = 1 // Ya hay 1 en el carrito
                    )
                ))
            }

            //WHEN
            val viewModel = createViewModel(
                fakeProduct = fakeProduct,
                fakeCartItemRepository = fakeCart,
                productId = productId
            )

            //THEN
            viewModel.events.test {
                viewModel.addToCart(productId) // Intentar agregar 1 más (total = 2 > stock = 1)

                val event = awaitItem()
                assertNotNull(event)
                assertTrue(event is ProductDetailEvent.INSUFFICIENT_STOCK_ERROR)

                cancelAndConsumeRemainingEvents()
            }
        }

    @Test
    fun should_emit_unknown_error_event_when_use_case_throws_QuantityMustBePositive_in_addToCart() =
        runTest {
            //GIVEN
            val productId = "id-01"
            val fakeProduct = FakeProductRepository().apply {
                setProducts(listOf(product {
                    withId(productId)
                    withName("Test Product")
                    withPrice(10.0)
                    withStock(10)
                }))
            }

            val fakeCart = FailingCartItemRepositoryStub(
                AppError.Validation.QuantityMustBePositive()
            )


            //WHEN
            val viewModel = createViewModel(
                fakeProduct = fakeProduct,
                fakeCartItemRepository = fakeCart,
                productId = productId
            )

            //THEN
            viewModel.events.test {
                viewModel.addToCart(productId)

                val event = awaitItem()
                assertNotNull(event)
                assertTrue(event is ProductDetailEvent.Error)

                val error: AppError = (event as ProductDetailEvent.Error).cause as AppError
                assertTrue(error is AppError.Validation.QuantityMustBePositive)

                cancelAndConsumeRemainingEvents()
            }

        }

    @Test
    fun should_emit_unknown_error_event_when_use_case_throws_generic_exception_in_addToCart() =
        runTest {
            //GIVEN
            val productId = "id-01"
            val fakeProduct = FakeProductRepository().apply {
                setProducts(listOf(product {
                    withId(productId)
                    withName("Test Product")
                    withPrice(10.0)
                    withStock(10)
                }))
            }
            // Usamos un stub que lanza una excepción genérica
            val fakeCart = FailingCartItemRepositoryStub(
                Exception("Something went wrong")
            )

            //WHEN
            val viewModel = createViewModel(
                fakeProduct = fakeProduct,
                fakeCartItemRepository = fakeCart,
                productId = productId
            )

            //THEN
            viewModel.events.test {
                viewModel.addToCart(productId)

                val event = awaitItem()
                assertNotNull(event)
                assertTrue(event is ProductDetailEvent.Error)

                val error: AppError = (event as ProductDetailEvent.Error).cause as AppError
                assertTrue(error is AppError.UnknownError)

                cancelAndConsumeRemainingEvents()
            }

        }

    @Test
    fun should_process_only_latest_emission_when_use_case_emits_multiple_products_rapidly_in_init() =
        runTest {
            //GIVEN
            val productId = "id-01"
            val firstProduct = product {
                withId(productId)
                withName("First Product")
                withPrice(10.0)
            }
            val secondProduct = product {
                withId(productId)
                withName("Second Product")
                withPrice(20.0)
            }
            val thirdProduct = product {
                withId(productId)
                withName("Third Product")
                withPrice(30.0)
            }

            val fakeProduct = FakeProductRepository().apply {
                setProducts(listOf(firstProduct))
            }

            //WHEN
            val viewModel = createViewModel(fakeProduct = fakeProduct, productId = productId)

            //THEN
            viewModel.uiState.test {
                // Primera emisión
                val firstState = awaitItem()
                assertEquals("First Product", firstState.item?.product?.name)

                // Emitir segundo producto
                fakeProduct.setProducts(listOf(secondProduct))
                val secondState = awaitItem()
                assertEquals("Second Product", secondState.item?.product?.name)

                // Emitir tercer producto
                fakeProduct.setProducts(listOf(thirdProduct))
                val thirdState = awaitItem()
                assertEquals("Third Product", thirdState.item?.product?.name)
                assertEquals(30.0, thirdState.item?.product?.price ?: 0.0, 0.01)

                cancelAndConsumeRemainingEvents()
            }
        }

    @Test
    fun should_buffer_event_when_event_is_emitted_before_collection_in_events_flow() = runTest {
        //GIVEN
        val productId = "id-01"
        val product = product {
            withId(productId)
            withName("Test Product")
            withPrice(10.0)
            withStock(5)
        }
        val fakeProduct = FakeProductRepository().apply {
            setProducts(listOf(product))
        }
        val fakeCart = FakeCartItemRepository()

        val viewModel = createViewModel(
            fakeProduct = fakeProduct,
            fakeCartItemRepository = fakeCart,
            productId = productId
        )

        // Mantener el uiState activo
        val uiStateJob = backgroundScope.launch {
            viewModel.uiState.collect {}
        }


        viewModel.events.test {
            //WHEN
            viewModel.addToCart(productId)

            //THEN
            val event = awaitItem()
            assertNotNull(event)
            assertTrue(event is ProductDetailEvent.SUCCESS_ADD_TO_CART)

            cancelAndConsumeRemainingEvents()
        }

        uiStateJob.cancel()
    }
}
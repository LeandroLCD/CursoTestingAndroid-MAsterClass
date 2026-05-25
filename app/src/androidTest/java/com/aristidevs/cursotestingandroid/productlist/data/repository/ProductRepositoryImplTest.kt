package com.aristidevs.cursotestingandroid.productlist.data.repository

import androidx.test.ext.junit.runners.AndroidJUnit4
import app.cash.turbine.test
import com.aristidevs.cursotestingandroid.core.domain.model.AppError
import com.aristidevs.cursotestingandroid.core.mockwebserver.MiniMarketApiOkhttpDispatcher
import com.aristidevs.cursotestingandroid.core.mockwebserver.rules.MockWebServerRule
import com.aristidevs.cursotestingandroid.core.utils.TestAssets.asAssets
import com.aristidevs.cursotestingandroid.core.utils.awaitMatches
import com.aristidevs.cursotestingandroid.productlist.domain.repository.ProductRepository
import dagger.hilt.android.testing.HiltAndroidRule
import dagger.hilt.android.testing.HiltAndroidTest
import kotlinx.coroutines.test.runTest
import okhttp3.mockwebserver.MockResponse
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import javax.inject.Inject
import kotlin.time.Duration.Companion.seconds

@HiltAndroidTest
@RunWith(AndroidJUnit4::class)
class ProductRepositoryImplTest {

    @get:Rule(order = 0)
    val mockWebServerRule = MockWebServerRule()

    @get:Rule(order = 1)
    val hiltRule = HiltAndroidRule(this)

    @Inject
    lateinit var productRepository: ProductRepository

    @Before
    fun setUp() {
        hiltRule.inject()
    }
    // ─── getProducts ─────────────────────────────────────────────────────────────

    @Test
    fun should_emit_list_from_database_then_products_after_successful_refresh_in_getProducts() =
        runTest {
            //GIVEN
            mockWebServerRule.localServer.enqueue(
                MockResponse()
                    .setResponseCode(200)
                    .setBody(
                        """{"products":[
                        {"id":"1","name":"Apple","category":"food","priceCents":100,"stock":5},
                        {"id":"2","name":"Banana","category":"drinks","priceCents":50,"stock":20}
                    ]}"""
                    )
            )

            //WHEN & THEN
            productRepository.getProducts().test {

                val withProducts = awaitMatches {
                    it.isNotEmpty()
                }
                assertEquals(2, withProducts.size)
                assertEquals("1", withProducts[0].id)
                assertEquals("Apple", withProducts[0].name)
                assertEquals("2", withProducts[1].id)
                assertEquals("Banana", withProducts[1].name)

                cancelAndConsumeRemainingEvents()
            }
        }

    @Test
    fun should_emit_only_empty_list_when_server_returns_404_in_getProducts() = runTest {
        //GIVEN
        mockWebServerRule.localServer.enqueue(
            MockResponse().setResponseCode(404)
        )

        //WHEN & THEN
        productRepository.getProducts().test {
            val emission = awaitItem()
            assertEquals(0, emission.size)
            cancelAndConsumeRemainingEvents()
        }
    }

    @Test
    fun should_emit_only_empty_list_when_server_returns_empty_product_in_getProducts() = runTest {
        //GIVEN
        mockWebServerRule.localServer.enqueue(
            MockResponse()
                .setResponseCode(200)
                .setBody(
                    """{"products":[]}"""
                )
        )

        //WHEN & THEN
        productRepository.getProducts().test {
            val emission = awaitItem()
            assertEquals(0, emission.size)
            cancelAndConsumeRemainingEvents()
        }
    }

    @Test
    fun should_filter_products_with_no_category_in_getProducts() = runTest {
        //GIVEN
        //TODO revisar
        mockWebServerRule.localServer.enqueue(
            MockResponse()
                .setResponseCode(200)
                .setBody(
                    """{"products":[
                        {"id":"1","name":"Apple","category":"food","priceCents":100,"stock":5},
                        {"id":"2","name":"NoCat","priceCents":50,"stock":1}
                    ]}"""
                )
        )

        //WHEN & THEN
        productRepository.refreshProduct()
        productRepository.getProducts().test(timeout = 6.seconds) {

            val withProducts = awaitMatches { it.isNotEmpty() }
            assertEquals(1, withProducts.size)
            assertEquals("1", withProducts[0].id)

            cancelAndConsumeRemainingEvents()
        }
    }

    // ─── refreshProduct ──────────────────────────────────────────────────────────//

    @Test
    fun should_fetch_and_persist_products_from_remote_in_refreshProduct() = runTest {
        //GIVEN
        mockWebServerRule.localServer.enqueue(
            MockResponse()
                .setResponseCode(200)
                .setBody(
                    """{"products":[
                        {"id":"10","name":"Mango","category":"fruit","priceCents":200,"stock":8}
                    ]}"""
                )
        )

        //WHEN
        productRepository.refreshProduct()

        //THEN
        productRepository.getProductById("10").test {
            val product = awaitItem()
            assertNotNull(product)
            assertEquals("10", product?.id)
            assertEquals("Mango", product?.name)
            cancelAndConsumeRemainingEvents()
        }
    }

    @Test
    fun should_throw_NotFoundError_when_server_returns_404_in_refreshProduct() = runTest {
        //GIVEN
        mockWebServerRule.localServer.enqueue(
            MockResponse().setResponseCode(404)
        )

        //WHEN
        val result = runCatching { productRepository.refreshProduct() }

        //THEN
        assertTrue(result.isFailure)
        assertTrue(result.exceptionOrNull() is AppError.NotFoundError)
    }

    @Test
    fun should_update_product_price_when_refresh_returns_only_product_A_with_increased_price_in_refreshProduct() =
        runTest {
            //GIVEN
            mockWebServerRule.localServer.enqueue(
                MockResponse()
                    .setResponseCode(200)
                    .setBody(
                        """{"products":[
                        {"id":"A","name":"Avocado","category":"fruit","priceCents":150,"stock":3},
                        {"id":"B","name":"Blueberry","category":"fruit","priceCents":300,"stock":15},
                        {"id":"C","name":"Cherry","category":"fruit","priceCents":400,"stock":7}
                    ]}"""
                    )
            )
            productRepository.refreshProduct()

            mockWebServerRule.localServer.enqueue(
                MockResponse()
                    .setResponseCode(200)
                    .setBody(
                        """{"products":[
                        {"id":"A","name":"Avocado","category":"fruit","priceCents":200,"stock":3}
                    ]}"""
                    )
            )

            //WHEN
            productRepository.refreshProduct()

            //THEN
            productRepository.getProductById("A").test {
                val product = awaitItem()
                assertNotNull(product)
                assertEquals("A", product?.id)
                assertEquals(2.0, product?.price)
                cancelAndConsumeRemainingEvents()
            }
        }

    @Test
    fun should_throw_NetworkError_when_server_returns_500_in_refreshProduct() = runTest {
        //GIVEN
        mockWebServerRule.localServer.enqueue(
            MockResponse().setResponseCode(500)
        )

        //WHEN
        val result = runCatching { productRepository.refreshProduct() }

        //THEN
        assertTrue(result.isFailure)
        assertTrue(result.exceptionOrNull() is AppError.NetworkError)
    }

    // ─── getProductById ──────────────────────────────────────────────────────────

    @Test
    fun should_emit_product_when_it_exists_in_getProductById() = runTest {
        //GIVEN
        mockWebServerRule.localServer.enqueue(
            MockResponse()
                .setResponseCode(200)
                .setBody(
                    """{"products":[
                        {"id":"99","name":"Kiwi","category":"fruit","priceCents":300,"stock":12}
                    ]}"""
                )
        )
        productRepository.refreshProduct()

        //WHEN & THEN
        productRepository.getProductById("99").test {
            val product = awaitItem()
            assertNotNull(product)
            assertEquals("99", product?.id)
            assertEquals("Kiwi", product?.name)
            assertEquals("fruit", product?.category)
            cancelAndConsumeRemainingEvents()
        }
    }

    @Test
    fun should_emit_null_when_product_does_not_exist_in_getProductById() = runTest {
        //GIVEN
        mockWebServerRule.localServer.enqueue(
            MockResponse()
                .setResponseCode(200)
                .setBody(
                    """{"products":[
                        {"id":"99","name":"Kiwi","category":"fruit","priceCents":300,"stock":12}
                    ]}"""
                )
        )
        productRepository.refreshProduct()
        //WHEN & THEN
        productRepository.getProductById("non-existent").test {
            val product = awaitItem()
            assertNull(product)
            cancelAndConsumeRemainingEvents()
        }
    }

    // ─── getProductsByIds ────────────────────────────────────────────────────────

    @Test
    fun should_emit_products_for_given_ids_in_getProductsByIds() = runTest {
        //GIVEN
        mockWebServerRule.localServer.enqueue(
            MockResponse()
                .setResponseCode(200)
                .setBody(
                    """{"products":[
                        {"id":"A","name":"Avocado","category":"fruit","priceCents":150,"stock":3},
                        {"id":"B","name":"Blueberry","category":"fruit","priceCents":300,"stock":15},
                        {"id":"C","name":"Cherry","category":"fruit","priceCents":400,"stock":7}
                    ]}"""
                )
        )
        productRepository.refreshProduct()

        //WHEN & THEN
        productRepository.getProductsByIds(setOf("A", "C")).test {
            val products = awaitItem()
            assertEquals(2, products.size)
            assertTrue(products.any { it.id == "A" })
            assertTrue(products.any { it.id == "C" })
            cancelAndConsumeRemainingEvents()
        }
    }

    @Test
    fun should_emit_empty_list_when_ids_set_is_empty_in_getProductsByIds() = runTest {
        //GIVEN
        val ids = emptySet<String>()

        //WHEN & THEN
        productRepository.getProductsByIds(ids).test {
            val products = awaitItem()
            assertEquals(0, products.size)
            cancelAndConsumeRemainingEvents()
        }
    }

    @Test
    fun should_emit_empty_list_when_none_of_the_ids_exist_in_getProductsByIds() = runTest {
        //GIVEN — DB vacía, sin refresh
        val ids = setOf("X", "Y", "Z")

        //WHEN & THEN
        productRepository.getProductsByIds(ids).test {
            val products = awaitItem()
            assertEquals(0, products.size)
            cancelAndConsumeRemainingEvents()
        }
    }
}
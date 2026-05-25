package com.aristidevs.cursotestingandroid.productlist.data.repository

import app.cash.turbine.test
import com.aristidevs.cursotestingandroid.core.TestDispatchersProvider
import com.aristidevs.cursotestingandroid.productlist.data.local.LocalDataSource
import com.aristidevs.cursotestingandroid.productlist.data.remote.RemoteDataSource
import com.aristidevs.cursotestingandroid.productlist.data.repository.builder.productEntity
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.just
import io.mockk.mockk
import io.mockk.runs
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Before
import org.junit.Rule
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)

class ProductRepositoryImplTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private lateinit var remoteDataSource: RemoteDataSource
    private lateinit var localDataSource: LocalDataSource
    private lateinit var repository: ProductRepositoryImpl

    @Before
    fun setUp() {
        remoteDataSource = mockk()
        localDataSource = mockk()
        repository = ProductRepositoryImpl(
            remoteDataSource = remoteDataSource,
            localDataSource = localDataSource,
            dispatchers = TestDispatchersProvider(mainDispatcherRule.testDispatcher)
        )
    }

    // ─── getProducts ─────────────────────────────────────────────────────────────

    @Test
    fun should_emit_mapped_products_when_local_data_source_has_products_in_getProducts() = runTest {
        //GIVEN
        val entity1 = productEntity { withId("1"); withName("Apple"); withCategory("food") }
        val entity2 = productEntity { withId("2"); withName("Banana"); withCategory("drinks") }
        every { localDataSource.getAllProducts() } returns flowOf(listOf(entity1, entity2))
        coEvery { remoteDataSource.getProducts() } returns Result.success(emptyList())
        coEvery { localDataSource.saveProducts(any()) } just runs

        //WHEN & THEN
        repository.getProducts().test {
            val items = awaitItem()
            assertEquals(2, items.size)
            assertEquals("1", items[0].id)
            assertEquals("Apple", items[0].name)
            assertEquals("2", items[1].id)
            assertEquals("Banana", items[1].name)
            cancelAndConsumeRemainingEvents()
        }
    }

    @Test
    fun should_emit_empty_list_when_local_data_source_has_no_products_in_getProducts() = runTest {
        //GIVEN
        every { localDataSource.getAllProducts() } returns flowOf(emptyList())
        coEvery { remoteDataSource.getProducts() } returns Result.success(emptyList())
        coEvery { localDataSource.saveProducts(any()) } just runs

        //WHEN & THEN
        repository.getProducts().test {
            val items = awaitItem()
            assertEquals(0, items.size)
            cancelAndConsumeRemainingEvents()
        }
    }

    @Test
    fun should_filter_out_products_with_null_category_in_getProducts() = runTest {
        //GIVEN
        val validEntity = productEntity { withId("1"); withCategory("food") }
        val nullCategoryEntity = productEntity { withId("2"); withCategory(null) }
        val emptyCategoryEntity = productEntity { withId("3"); withCategory("") }
        every { localDataSource.getAllProducts() } returns flowOf(
            listOf(validEntity, nullCategoryEntity, emptyCategoryEntity)
        )
        coEvery { remoteDataSource.getProducts() } returns Result.success(emptyList())
        coEvery { localDataSource.saveProducts(any()) } just runs

        //WHEN & THEN
        repository.getProducts().test {
            val items = awaitItem()
            assertEquals(1, items.size)
            assertEquals("1", items[0].id)
            cancelAndConsumeRemainingEvents()
        }
    }

    @Test
    fun should_trigger_refresh_and_save_products_to_local_when_collecting_in_getProducts() = runTest {
        //GIVEN
        every { localDataSource.getAllProducts() } returns flowOf(emptyList())
        coEvery { remoteDataSource.getProducts() } returns Result.success(emptyList())
        coEvery { localDataSource.saveProducts(any()) } just runs

        //WHEN
        repository.getProducts().test {
            awaitItem()
            advanceUntilIdle()
            cancelAndConsumeRemainingEvents()
        }

        //THEN
        coVerify { remoteDataSource.getProducts() }
        coVerify { localDataSource.saveProducts(any()) }
    }

    @Test
    fun should_not_trigger_refresh_twice_when_collecting_concurrently_in_getProducts() = runTest {
        //GIVEN
        every { localDataSource.getAllProducts() } returns flowOf(emptyList())
        coEvery { remoteDataSource.getProducts() } returns Result.success(emptyList())
        coEvery { localDataSource.saveProducts(any()) } just runs

        //WHEN
        repository.getProducts().test {
            awaitItem()
            advanceUntilIdle()
            cancelAndConsumeRemainingEvents()
        }
        repository.getProducts().test {
            awaitItem()
            advanceUntilIdle()
            cancelAndConsumeRemainingEvents()
        }

        //THEN — el mutex solo permite una ejecución simultánea; la segunda es ignorada
        coVerify(atMost = 2) { remoteDataSource.getProducts() }
    }

    // ─── getProductById ──────────────────────────────────────────────────────────

    @Test
    fun should_emit_mapped_product_when_product_exists_in_getProductById() = runTest {
        //GIVEN
        val productId = "1"
        val entity = productEntity { withId(productId); withName("Apple"); withCategory("food") }
        every { localDataSource.getProductById(productId) } returns flowOf(entity)

        //WHEN & THEN
        repository.getProductById(productId).test {
            val product = awaitItem()
            assertEquals(productId, product?.id)
            assertEquals("Apple", product?.name)
            assertEquals("food", product?.category)
            cancelAndConsumeRemainingEvents()
        }
    }

    @Test
    fun should_emit_null_when_product_does_not_exist_in_getProductById() = runTest {
        //GIVEN
        val productId = "non-existent"
        every { localDataSource.getProductById(productId) } returns flowOf(null)

        //WHEN & THEN
        repository.getProductById(productId).test {
            val product = awaitItem()
            assertNull(product)
            cancelAndConsumeRemainingEvents()
        }
    }

    @Test
    fun should_emit_null_when_product_entity_has_null_category_in_getProductById() = runTest {
        //GIVEN
        val productId = "1"
        val entity = productEntity { withId(productId); withCategory(null) }
        every { localDataSource.getProductById(productId) } returns flowOf(entity)

        //WHEN & THEN
        repository.getProductById(productId).test {
            val product = awaitItem()
            assertNull(product)
            cancelAndConsumeRemainingEvents()
        }
    }

    // ─── refreshProduct ──────────────────────────────────────────────────────────

    @Test
    fun should_fetch_remote_products_and_save_to_local_in_refreshProduct() = runTest {
        //GIVEN
        val remoteProducts = listOf(
            com.aristidevs.cursotestingandroid.productlist.data.remote.builder.productResponse {
                withId("1"); withName("Apple"); withCategory("food"); withPriceCents(200)
            }
        )
        coEvery { remoteDataSource.getProducts() } returns Result.success(remoteProducts)
        coEvery { localDataSource.saveProducts(any()) } just runs

        //WHEN
        repository.refreshProduct()

        //THEN
        coVerify { remoteDataSource.getProducts() }
        coVerify {
            localDataSource.saveProducts(
                match { entities -> entities.size == 1 && entities[0].id == "1" }
            )
        }
    }

    @Test
    fun should_throw_exception_when_remote_returns_failure_in_refreshProduct() = runTest {
        //GIVEN
        val error = RuntimeException("Network error")
        coEvery { remoteDataSource.getProducts() } returns Result.failure(error)

        //WHEN
        val result = runCatching { repository.refreshProduct() }

        //THEN
        assert(result.isFailure)
        assertEquals("Network error", result.exceptionOrNull()?.message)
    }

    // ─── getProductsByIds ────────────────────────────────────────────────────────

    @Test
    fun should_emit_mapped_products_for_given_ids_in_getProductsByIds() = runTest {
        //GIVEN
        val ids = setOf("1", "2")
        val entity1 = productEntity { withId("1"); withCategory("food") }
        val entity2 = productEntity { withId("2"); withCategory("drinks") }
        every { localDataSource.getProductsByIds(ids) } returns flowOf(listOf(entity1, entity2))

        //WHEN & THEN
        repository.getProductsByIds(ids).test {
            val items = awaitItem()
            assertEquals(2, items.size)
            assertEquals("1", items[0].id)
            assertEquals("2", items[1].id)
            cancelAndConsumeRemainingEvents()
        }
    }

    @Test
    fun should_emit_empty_list_when_ids_set_is_empty_in_getProductsByIds() = runTest {
        //GIVEN
        val ids = emptySet<String>()
        every { localDataSource.getProductsByIds(ids) } returns flowOf(emptyList())

        //WHEN & THEN
        repository.getProductsByIds(ids).test {
            val items = awaitItem()
            assertEquals(0, items.size)
            cancelAndConsumeRemainingEvents()
        }
    }
}




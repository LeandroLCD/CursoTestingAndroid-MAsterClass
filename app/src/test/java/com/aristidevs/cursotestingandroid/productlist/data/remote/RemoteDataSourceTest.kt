package com.aristidevs.cursotestingandroid.productlist.data.remote

import com.aristidevs.cursotestingandroid.core.BaseUnitTest
import com.aristidevs.cursotestingandroid.core.domain.model.AppError
import com.aristidevs.cursotestingandroid.productlist.data.remote.builder.productResponse
import com.aristidevs.cursotestingandroid.productlist.data.remote.builder.promotionResponse
import kotlinx.coroutines.test.runTest
import kotlinx.serialization.json.Json
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import retrofit2.Retrofit
import retrofit2.converter.kotlinx.serialization.asConverterFactory

class RemoteDataSourceTest : BaseUnitTest() {

    private lateinit var mockWebServer: MockWebServer
    private lateinit var remoteDataSource: RemoteDataSource

    @Before
    fun setUp() {
        mockWebServer = MockWebServer()
        mockWebServer.start()

        val json = Json {
            ignoreUnknownKeys = true
            isLenient = true
            coerceInputValues = true
        }

        val retrofit = Retrofit.Builder()
            .baseUrl(mockWebServer.url("/"))
            .client(OkHttpClient())
            .addConverterFactory(json.asConverterFactory("application/json".toMediaType()))
            .build()

        val apiService = retrofit.create(MiniMarketApiService::class.java)
        remoteDataSource = RemoteDataSource(apiService)
    }

    @After
    fun tearDown() {
        mockWebServer.shutdown()
    }

    // ─── getProducts ────────────────────────────────────────────────────────────

    @Test
    fun should_return_success_with_products_when_server_responds_200_in_getProducts() = runTest {
        //GIVEN
        val product1 = productResponse { withId("1"); withName("Apple") }
        val product2 = productResponse { withId("2"); withName("Banana") }
        mockWebServer.enqueue(
            MockResponse()
                .setResponseCode(200)
                .setBody(getResponseFromJson("products_200.json"))
        )

        //WHEN
        val result = remoteDataSource.getProducts()

        //THEN
        assertTrue(result.isSuccess)
        val products = result.getOrNull()
        assertEquals(2, products?.size)
        assertEquals(product1.id, products?.get(0)?.id)
        assertEquals(product1.name, products?.get(0)?.name)
        assertEquals(product2.id, products?.get(1)?.id)
        assertEquals(product2.name, products?.get(1)?.name)
    }

    @Test
    fun should_return_empty_list_when_server_responds_200_with_no_products_in_getProducts() = runTest {
        //GIVEN
        mockWebServer.enqueue(
            MockResponse()
                .setResponseCode(200)
                .setBody("""{"products":[]}""")
        )

        //WHEN
        val result = remoteDataSource.getProducts()

        //THEN
        assertTrue(result.isSuccess)
        assertEquals(0, result.getOrNull()?.size)
    }

    @Test
    fun should_return_NotFoundError_when_server_responds_404_in_getProducts() = runTest {
        //GIVEN
        mockWebServer.enqueue(
            MockResponse().setResponseCode(404)
        )

        //WHEN
        val result = remoteDataSource.getProducts()

        //THEN
        assertTrue(result.isFailure)
        assertTrue(result.exceptionOrNull() is AppError.NotFoundError)
    }

    @Test
    fun should_return_NetworkError_when_server_responds_500_in_getProducts() = runTest {
        //GIVEN
        mockWebServer.enqueue(
            MockResponse().setResponseCode(500)
        )

        //WHEN
        val result = remoteDataSource.getProducts()

        //THEN
        assertTrue(result.isFailure)
        assertTrue(result.exceptionOrNull() is AppError.NetworkError)
    }

    @Test
    fun should_return_NetworkError_when_server_closes_connection_unexpectedly_in_getProducts() = runTest {
        //GIVEN
        mockWebServer.enqueue(
            MockResponse().setSocketPolicy(okhttp3.mockwebserver.SocketPolicy.DISCONNECT_AFTER_REQUEST)
        )

        //WHEN
        val result = remoteDataSource.getProducts()

        //THEN
        assertTrue(result.isFailure)
        assertTrue(result.exceptionOrNull() is AppError.NetworkError)
    }

    // ─── getPromotions ──────────────────────────────────────────────────────────

    @Test
    fun should_request_correct_url_and_method_GET_when_fetching_promotions_in_getPromotions() = runTest {
        //GIVEN
        mockWebServer.enqueue(
            MockResponse()
                .setResponseCode(200)
                .setBody(getResponseFromJson("promotions_200.json"))
        )

        //WHEN
        remoteDataSource.getPromotions()

        //THEN
        val request = mockWebServer.takeRequest()
        assertEquals("GET", request.method)
        assertEquals("/data/promotions.json", request.path)
    }

    @Test
    fun should_return_success_with_promotions_when_server_responds_200_in_getPromotions() = runTest {
        //GIVEN
        val promo1 = promotionResponse { withId("p1"); withProductId("1"); withType("PERCENT"); withPercent(20) }
        mockWebServer.enqueue(
            MockResponse()
                .setResponseCode(200)
                .setBody(getResponseFromJson("promotions_200.json"))
        )

        //WHEN
        val result = remoteDataSource.getPromotions()

        //THEN
        assertTrue(result.isSuccess)
        val promotions = result.getOrNull()
        assertEquals(1, promotions?.size)
        assertEquals(promo1.id, promotions?.get(0)?.id)
        assertEquals(promo1.productId, promotions?.get(0)?.productId)
        assertEquals(promo1.type, promotions?.get(0)?.type)
        assertEquals(promo1.percent, promotions?.get(0)?.percent)
    }

    @Test
    fun should_return_empty_list_when_server_responds_200_with_no_promotions_in_getPromotions() = runTest {
        //GIVEN
        mockWebServer.enqueue(
            MockResponse()
                .setResponseCode(200)
                .setBody("""{"promotions":[]}""")
        )

        //WHEN
        val result = remoteDataSource.getPromotions()

        //THEN
        assertTrue(result.isSuccess)
        assertEquals(0, result.getOrNull()?.size)
    }

    @Test
    fun should_return_NotFoundError_when_server_responds_404_in_getPromotions() = runTest {
        //GIVEN
        mockWebServer.enqueue(
            MockResponse().setResponseCode(404)
        )

        //WHEN
        val result = remoteDataSource.getPromotions()

        //THEN
        assertTrue(result.isFailure)
        assertTrue(result.exceptionOrNull() is AppError.NotFoundError)
    }

    @Test
    fun should_return_NetworkError_when_server_responds_500_in_getPromotions() = runTest {
        //GIVEN
        mockWebServer.enqueue(
            MockResponse().setResponseCode(500)
        )

        //WHEN
        val result = remoteDataSource.getPromotions()

        //THEN
        assertTrue(result.isFailure)
        assertTrue(result.exceptionOrNull() is AppError.NetworkError)
    }

    @Test
    fun should_return_NetworkError_when_server_closes_connection_unexpectedly_in_getPromotions() = runTest {
        //GIVEN
        mockWebServer.enqueue(
            MockResponse().setSocketPolicy(okhttp3.mockwebserver.SocketPolicy.DISCONNECT_AFTER_REQUEST)
        )

        //WHEN
        val result = remoteDataSource.getPromotions()

        //THEN
        assertTrue(result.isFailure)
        assertTrue(result.exceptionOrNull() is AppError.NetworkError)
    }
}
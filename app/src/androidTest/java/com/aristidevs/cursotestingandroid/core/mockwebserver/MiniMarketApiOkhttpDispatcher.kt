package com.aristidevs.cursotestingandroid.core.mockwebserver

import okhttp3.mockwebserver.Dispatcher
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.RecordedRequest

class MiniMarketApiOkhttpDispatcher(
    private val products: String,
    private val promotions: String = """{promotions: []}"""
): Dispatcher() {
    override fun dispatch(request: RecordedRequest): MockResponse {
        return when {
            request.path?.contains("products.json") == true ->
                MockResponse()
                    .setResponseCode(200)
                    .setBody(products)
                    .addHeader("Content-Type", "application/json")
            request.path?.contains("promotions.json") == true ->
                MockResponse()
                    .setResponseCode(200)
                    .setBody(promotions)
                    .addHeader("Content-Type", "application/json")
            else -> MockResponse().setResponseCode(404).setBody("Not found")
        }
    }
}
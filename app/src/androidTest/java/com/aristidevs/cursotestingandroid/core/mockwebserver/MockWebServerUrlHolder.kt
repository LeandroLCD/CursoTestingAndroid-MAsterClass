package com.aristidevs.cursotestingandroid.core.mockwebserver

object MockWebServerUrlHolder {
    private const val BASE_URL = "http://localhost:8080/"
    var baseUrl = BASE_URL

    fun resetUrl() {
        baseUrl = BASE_URL
    }
}
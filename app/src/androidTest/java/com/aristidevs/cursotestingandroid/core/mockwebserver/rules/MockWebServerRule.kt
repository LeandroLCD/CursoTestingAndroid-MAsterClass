package com.aristidevs.cursotestingandroid.core.mockwebserver.rules

import com.aristidevs.cursotestingandroid.core.mockwebserver.MockWebServerUrlHolder
import okhttp3.mockwebserver.MockWebServer
import org.junit.rules.TestWatcher
import org.junit.runner.Description

class MockWebServerRule: TestWatcher() {
    val mockWebServer = MockWebServer()
    override fun starting(description: Description?) {
        super.starting(description)
        mockWebServer.start(8080)
        MockWebServerUrlHolder.baseUrl = mockWebServer.url("/").toString()
        println("MockWebServerUrlHolder.baseUrl: ${MockWebServerUrlHolder.baseUrl}")
    }

    override fun finished(description: Description?) {
        mockWebServer.shutdown()
        MockWebServerUrlHolder.resetUrl()
        super.finished(description)
    }
}
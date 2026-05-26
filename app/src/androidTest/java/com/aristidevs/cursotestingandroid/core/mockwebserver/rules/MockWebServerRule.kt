package com.aristidevs.cursotestingandroid.core.mockwebserver.rules

import com.aristidevs.cursotestingandroid.core.mockwebserver.MockWebServerUrlHolder
import okhttp3.mockwebserver.MockWebServer
import org.junit.rules.TestWatcher
import org.junit.runner.Description

class MockWebServerRule : TestWatcher() {
    val localServer = MockWebServer()
    override fun starting(description: Description?) {
        super.starting(description)
        localServer.start()
        MockWebServerUrlHolder.baseUrl = localServer.url("/").toString()
    }

    override fun finished(description: Description?) {
        localServer.shutdown()
        MockWebServerUrlHolder.resetUrl()
        super.finished(description)
    }
}
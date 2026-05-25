package com.aristidevs.cursotestingandroid.core.utils

import app.cash.turbine.TurbineTestContext
import kotlinx.coroutines.withTimeoutOrNull
import kotlin.time.Duration
import kotlin.time.Duration.Companion.seconds

suspend fun <T> TurbineTestContext<T>.awaitMatches(timeout: Duration = 3.seconds, predicate: (T) -> Boolean): T {
    return withTimeoutOrNull(timeout) {
        while (true) {
            val item = awaitItem()
            if (predicate(item)) {
                return@withTimeoutOrNull item
            }
        }
        @Suppress("UNREACHABLE_CODE")
        error("unreachable")
    } ?: error("Timed out waiting for item matching the predicate")
}

suspend fun <T> TurbineTestContext<T>.awaitMatches(predicate: (T) -> Boolean): T {
    while (true) {
        val item = awaitItem()
        println("item: $item")
        if (predicate(item)) {
            return item
        }
    }
}
package com.aristidevs.cursotestingandroid.core.utils

import app.cash.turbine.TurbineTestContext

suspend fun <T> TurbineTestContext<T>.awaitMatches(predicate: (T) -> Boolean): T {
    while (true) {
        val item = awaitItem()
        println("awaitMatches item: $item")
        if (predicate(item)) {
            return item
        }
    }
}

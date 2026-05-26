package com.aristidevs.cursotestingandroid.core.utils

import androidx.test.platform.app.InstrumentationRegistry

object TestAssets {
    fun readJson(fileName: String): String {
        val context = InstrumentationRegistry.getInstrumentation().context
        return context.assets.open(fileName).bufferedReader().use { it.readText() }
    }
    fun String.asAssets(): String = readJson(this)
}
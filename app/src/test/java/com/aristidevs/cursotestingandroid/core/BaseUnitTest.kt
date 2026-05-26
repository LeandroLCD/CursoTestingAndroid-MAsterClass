package com.aristidevs.cursotestingandroid.core

import org.junit.Assert.assertNotNull
import java.io.BufferedReader

abstract class BaseUnitTest {

    fun getResponseFromJson(res: String): String {
        val classLoader = this::class.java.classLoader
        // Carga el archivo desde la carpeta de resources
        val inputStream = classLoader?.getResourceAsStream(res)
        // en junit4
        assertNotNull("El archivo JSON '$res' no fue encontrado", inputStream)
        // Read the file content
        return inputStream!!.bufferedReader().use(BufferedReader::readText)
    }
}


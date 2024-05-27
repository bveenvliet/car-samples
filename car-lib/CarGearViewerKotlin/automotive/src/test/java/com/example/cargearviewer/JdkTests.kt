package com.example.cargearviewer

import org.junit.Assert.assertEquals
import org.junit.Test

class JdkTests {

    @Test
    fun `check if JDK version is correct`() {
        assertEquals(
            "17",
            System.getProperty("java.version")?.split(".")?.get(0) ?: "error"
        )
    }
}

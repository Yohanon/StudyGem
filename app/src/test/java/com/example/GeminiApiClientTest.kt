package com.example

import com.example.data.api.GeminiApiClient
import org.junit.Assert.assertEquals
import org.junit.Test

class GeminiApiClientTest {

    private val client = GeminiApiClient()

    @Test
    fun sanitizeApiKey_cleansWhitespaceAndQuotes() {
        val messyKey = " \"  AIzaSyTestKey_12345 \n\t\" "
        val cleaned = client.sanitizeApiKey(messyKey)
        assertEquals("AIzaSyTestKey_12345", cleaned)
    }

    @Test
    fun sanitizeApiKey_handlesSingleQuotesAndBackticks() {
        val singleQuoted = "'AIzaSyKeySingle'"
        val backticked = "`AIzaSyKeyBacktick`"
        assertEquals("AIzaSyKeySingle", client.sanitizeApiKey(singleQuoted))
        assertEquals("AIzaSyKeyBacktick", client.sanitizeApiKey(backticked))
    }
}

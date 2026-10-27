package com.example

import com.example.data.preferences.SecurePreferencesManager
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class SecurePreferencesManagerTest {

    @Test
    fun sanitizeApiKey_removesLeadingAndTrailingWhitespace() {
        val raw = "   AIzaSyAbc123_xyz   \n\t"
        val sanitized = SecurePreferencesManager.sanitizeApiKey(raw)
        assertEquals("AIzaSyAbc123_xyz", sanitized)
    }

    @Test
    fun sanitizeApiKey_removesEnclosingQuotes() {
        val doubleQuotes = "\"AIzaSyAbc123_xyz\""
        val singleQuotes = "'AIzaSyAbc123_xyz'"
        assertEquals("AIzaSyAbc123_xyz", SecurePreferencesManager.sanitizeApiKey(doubleQuotes))
        assertEquals("AIzaSyAbc123_xyz", SecurePreferencesManager.sanitizeApiKey(singleQuotes))
    }

    @Test
    fun sanitizeApiKey_removesInternalNewlines() {
        val multiline = "AIzaSy\nAbc123\r_xyz"
        assertEquals("AIzaSyAbc123_xyz", SecurePreferencesManager.sanitizeApiKey(multiline))
    }

    @Test
    fun validateKeyFormat_identifiesValidAndInvalidKeys() {
        assertTrue(SecurePreferencesManager.isValidKeyFormat("AIzaSyA1B2C3D4E5F6G7H8I9J0K1L2M3N4O5P6"))
        assertFalse(SecurePreferencesManager.isValidKeyFormat(""))
        assertFalse(SecurePreferencesManager.isValidKeyFormat("short"))
        assertFalse(SecurePreferencesManager.isValidKeyFormat("   "))
    }
}

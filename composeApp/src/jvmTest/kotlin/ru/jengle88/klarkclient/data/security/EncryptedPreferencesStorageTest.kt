package ru.jengle88.klarkclient.data.security

import java.util.prefs.Preferences
import kotlin.test.AfterTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull
import kotlin.test.assertTrue

class EncryptedPreferencesStorageTest {
    @AfterTest
    fun cleanup() {
        // Clean up the shared encryption key to prevent test pollution
        val preferences = Preferences.userNodeForPackage(EncryptedPreferencesStorage::class.java)
        preferences.remove("_secure_storage_master_key")
        preferences.flush()
    }
    @Test
    fun `storage is available`() {
        val storage = EncryptedPreferencesStorage("TestService")
        assertTrue(storage.isAvailable())
    }

    @Test
    fun `store and retrieve value`() {
        val storage = EncryptedPreferencesStorage("TestService")
        val key = "test_key_${System.currentTimeMillis()}"
        val value = "test_value_123"

        storage.store(key, value)
        val retrieved = storage.retrieve(key)

        assertEquals(value, retrieved)

        // Clean up
        storage.remove(key)
    }

    @Test
    fun `retrieve non-existent key returns null`() {
        val storage = EncryptedPreferencesStorage("TestService")
        val key = "non_existent_key_${System.currentTimeMillis()}"

        val retrieved = storage.retrieve(key)

        assertNull(retrieved)
    }

    @Test
    fun `remove value`() {
        val storage = EncryptedPreferencesStorage("TestService")
        val key = "test_key_remove_${System.currentTimeMillis()}"
        val value = "test_value"

        storage.store(key, value)
        storage.remove(key)
        val retrieved = storage.retrieve(key)

        assertNull(retrieved)
    }

    @Test
    fun `store multiple values`() {
        val storage = EncryptedPreferencesStorage("TestService")
        val timestamp = System.currentTimeMillis()
        val key1 = "test_key1_$timestamp"
        val key2 = "test_key2_$timestamp"
        val value1 = "value1"
        val value2 = "value2"

        storage.store(key1, value1)
        storage.store(key2, value2)

        assertEquals(value1, storage.retrieve(key1))
        assertEquals(value2, storage.retrieve(key2))

        // Clean up
        storage.remove(key1)
        storage.remove(key2)
    }

    @Test
    fun `overwrite existing value`() {
        val storage = EncryptedPreferencesStorage("TestService")
        val key = "test_key_overwrite_${System.currentTimeMillis()}"
        val value1 = "original_value"
        val value2 = "updated_value"

        storage.store(key, value1)
        storage.store(key, value2)
        val retrieved = storage.retrieve(key)

        assertEquals(value2, retrieved)

        // Clean up
        storage.remove(key)
    }

    @Test
    fun `store and retrieve sensitive data`() {
        val storage = EncryptedPreferencesStorage("TestService")
        val key = "test_sensitive_${System.currentTimeMillis()}"
        val sensitiveValue = "Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.test.token"

        storage.store(key, sensitiveValue)
        val retrieved = storage.retrieve(key)

        assertEquals(sensitiveValue, retrieved)

        // Clean up
        storage.remove(key)
    }
}

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

    @Test
    fun `data is encrypted in underlying storage`() {
        val storage = EncryptedPreferencesStorage("TestService")
        val key = "test_encryption_${System.currentTimeMillis()}"
        val plaintext = "my_secret_token"

        storage.store(key, plaintext)

        // Directly access preferences to verify encryption
        val preferences = Preferences.userNodeForPackage(EncryptedPreferencesStorage::class.java)
        val storedValue = preferences.get(key, null)

        // Stored value should not be null
        assertTrue(storedValue != null, "Stored value should exist")
        
        // Stored value should not equal plaintext (i.e., it's encrypted)
        assertTrue(storedValue != plaintext, "Stored value should be encrypted, not plaintext")
        
        // Stored value should look like Base64 (contains only Base64 characters)
        assertTrue(storedValue!!.matches(Regex("^[A-Za-z0-9+/]+=*$")), "Stored value should be Base64 encoded")

        // Clean up
        storage.remove(key)
    }

    @Test
    fun `retrieve handles corrupted encrypted data`() {
        val storage = EncryptedPreferencesStorage("TestService")
        val key = "test_corrupted_${System.currentTimeMillis()}"

        // Store valid data first
        storage.store(key, "valid_data")

        // Directly corrupt the data in preferences
        val preferences = Preferences.userNodeForPackage(EncryptedPreferencesStorage::class.java)
        preferences.put(key, "this_is_not_valid_base64_!@#$%")
        preferences.flush()

        // Attempt to retrieve should throw SecureStorageException
        try {
            storage.retrieve(key)
            assertTrue(false, "Should have thrown SecureStorageException")
        } catch (e: SecureStorageException) {
            // Expected
            assertTrue(e.message?.contains("Failed to retrieve") == true)
        }

        // Clean up
        storage.remove(key)
    }

    @Test
    fun `retrieve handles truncated encrypted data`() {
        val storage = EncryptedPreferencesStorage("TestService")
        val key = "test_truncated_${System.currentTimeMillis()}"

        // Store valid data first
        storage.store(key, "valid_data")

        // Directly truncate the data in preferences (Base64 "AA==" is too short for IV + encrypted data)
        val preferences = Preferences.userNodeForPackage(EncryptedPreferencesStorage::class.java)
        preferences.put(key, "AA==")
        preferences.flush()

        // Attempt to retrieve should throw SecureStorageException
        try {
            storage.retrieve(key)
            assertTrue(false, "Should have thrown SecureStorageException")
        } catch (e: SecureStorageException) {
            // Expected
            assertTrue(e.message?.contains("Failed to retrieve") == true)
        }

        // Clean up
        storage.remove(key)
    }

    @Test
    fun `retrieve handles tampered authentication tag`() {
        val storage = EncryptedPreferencesStorage("TestService")
        val key = "test_tampered_${System.currentTimeMillis()}"

        // Store valid data first
        storage.store(key, "valid_data")

        // Get the encrypted value and tamper with it
        val preferences = Preferences.userNodeForPackage(EncryptedPreferencesStorage::class.java)
        val encrypted = preferences.get(key, null)!!
        
        // Decode, flip a bit in the authentication tag (last few bytes), and re-encode
        val decoded = java.util.Base64.getDecoder().decode(encrypted)
        if (decoded.isNotEmpty()) {
            decoded[decoded.size - 1] = (decoded[decoded.size - 1].toInt() xor 1).toByte()
            val tampered = java.util.Base64.getEncoder().encodeToString(decoded)
            preferences.put(key, tampered)
            preferences.flush()

            // Attempt to retrieve should throw SecureStorageException (due to AEADBadTagException)
            try {
                storage.retrieve(key)
                assertTrue(false, "Should have thrown SecureStorageException for tampered data")
            } catch (e: SecureStorageException) {
                // Expected
                assertTrue(e.message?.contains("Failed to retrieve") == true)
            }
        }

        // Clean up
        storage.remove(key)
    }

    @Test
    fun `retrieve fails after encryption key is changed`() {
        val storage1 = EncryptedPreferencesStorage("TestService")
        val key = "test_key_change_${System.currentTimeMillis()}"
        val value = "test_value"

        // Store data with first storage instance
        storage1.store(key, value)

        // Remove the encryption key to force generation of a new one
        val preferences = Preferences.userNodeForPackage(EncryptedPreferencesStorage::class.java)
        preferences.remove("_secure_storage_master_key")
        preferences.flush()

        // Create new storage instance (will generate new key)
        val storage2 = EncryptedPreferencesStorage("TestService")

        // Attempt to retrieve with different key should fail
        try {
            storage2.retrieve(key)
            assertTrue(false, "Should have thrown SecureStorageException with different key")
        } catch (e: SecureStorageException) {
            // Expected - can't decrypt with different key
            assertTrue(e.message?.contains("Failed to retrieve") == true)
        }

        // Clean up
        storage2.remove(key)
    }
}

package ru.jengle88.klarkclient.data.security

import java.util.Base64
import java.util.prefs.Preferences
import javax.crypto.Cipher
import javax.crypto.KeyGenerator
import javax.crypto.SecretKey
import javax.crypto.spec.GCMParameterSpec
import javax.crypto.spec.SecretKeySpec

/**
 * Secure storage implementation using AES-GCM encryption.
 *
 * This implementation encrypts sensitive data before storing it in Java Preferences.
 * The encryption key is generated per-user and stored in a platform-specific location.
 *
 * While this is more secure than plain text storage, platform-specific implementations
 * (Windows Credential Manager, macOS Keychain, Linux Secret Service) would provide
 * better security. This implementation serves as a cross-platform fallback.
 */
class EncryptedPreferencesStorage(
    private val serviceName: String = "KlarkClient",
) : SecureStorage {
    private val preferences = Preferences.userNodeForPackage(EncryptedPreferencesStorage::class.java)
    private val cipher = Cipher.getInstance(TRANSFORMATION)

    // Lazy initialization of the encryption key
    private val encryptionKey: SecretKey by lazy {
        getOrCreateKey()
    }

    override fun store(
        key: String,
        value: String,
    ) {
        try {
            val encrypted = encrypt(value)
            preferences.put(key, encrypted)
            preferences.flush()
        } catch (e: Exception) {
            throw SecureStorageException("Failed to store value for key: $key", e)
        }
    }

    override fun retrieve(key: String): String? {
        return try {
            val encrypted = preferences.get(key, null) ?: return null
            decrypt(encrypted)
        } catch (e: Exception) {
            throw SecureStorageException("Failed to retrieve value for key: $key", e)
        }
    }

    override fun remove(key: String) {
        try {
            preferences.remove(key)
            preferences.flush()
        } catch (e: Exception) {
            throw SecureStorageException("Failed to remove value for key: $key", e)
        }
    }

    override fun isAvailable(): Boolean {
        return try {
            // Test if we can access preferences and crypto
            encryptionKey
            true
        } catch (e: Exception) {
            false
        }
    }

    private fun encrypt(plaintext: String): String {
        cipher.init(Cipher.ENCRYPT_MODE, encryptionKey)
        val iv = cipher.iv
        val encrypted = cipher.doFinal(plaintext.toByteArray(Charsets.UTF_8))

        // Combine IV and encrypted data
        val combined = iv + encrypted
        return Base64.getEncoder().encodeToString(combined)
    }

    private fun decrypt(ciphertext: String): String {
        val combined = Base64.getDecoder().decode(ciphertext)

        // Extract IV and encrypted data
        val iv = combined.copyOfRange(0, GCM_IV_LENGTH)
        val encrypted = combined.copyOfRange(GCM_IV_LENGTH, combined.size)

        val spec = GCMParameterSpec(GCM_TAG_LENGTH, iv)
        cipher.init(Cipher.DECRYPT_MODE, encryptionKey, spec)

        val decrypted = cipher.doFinal(encrypted)
        return String(decrypted, Charsets.UTF_8)
    }

    private fun getOrCreateKey(): SecretKey {
        // Try to retrieve existing key
        val keyString = preferences.get(KEY_STORAGE_KEY, null)

        return if (keyString != null) {
            val keyBytes = Base64.getDecoder().decode(keyString)
            SecretKeySpec(keyBytes, ALGORITHM)
        } else {
            // Generate new key
            val keyGen = KeyGenerator.getInstance(ALGORITHM)
            keyGen.init(KEY_SIZE)
            val key = keyGen.generateKey()

            // Store the key
            val keyString = Base64.getEncoder().encodeToString(key.encoded)
            preferences.put(KEY_STORAGE_KEY, keyString)
            preferences.flush()

            key
        }
    }

    companion object {
        private const val ALGORITHM = "AES"
        private const val TRANSFORMATION = "AES/GCM/NoPadding"
        private const val KEY_SIZE = 256
        private const val GCM_IV_LENGTH = 12
        private const val GCM_TAG_LENGTH = 128
        private const val KEY_STORAGE_KEY = "_secure_storage_master_key"
    }
}

/**
 * Factory for creating the appropriate SecureStorage implementation for the current platform.
 */
object SecureStorageFactory {
    /**
     * Creates a SecureStorage instance suitable for the current platform.
     *
     * Currently returns EncryptedPreferencesStorage which works across all platforms.
     * Future versions may add platform-specific implementations like Windows Credential Manager,
     * macOS Keychain, or Linux Secret Service.
     */
    fun create(serviceName: String = "KlarkClient"): SecureStorage {
        return EncryptedPreferencesStorage(serviceName)
    }
}

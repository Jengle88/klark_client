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
 * ## Security Considerations
 *
 * While this provides better security than plain text storage, it has some limitations:
 * - The master encryption key is stored in the same preferences store as the encrypted data.
 *   This means an attacker with file system access could potentially extract both the key
 *   and encrypted data.
 * - Platform-specific implementations (Windows Credential Manager, macOS Keychain,
 *   Linux Secret Service) would provide stronger security by using OS-level key protection.
 * - This implementation serves as a cross-platform fallback that is significantly more
 *   secure than plain text storage.
 *
 * ## Encryption Details
 *
 * - Algorithm: AES-GCM (Galois/Counter Mode)
 * - Key size: 256 bits
 * - Authentication tag: 128 bits
 * - IV size: 96 bits (12 bytes)
 */
class EncryptedPreferencesStorage(
    private val serviceName: String = "KlarkClient",
) : SecureStorage {
    private val preferences = Preferences.userNodeForPackage(EncryptedPreferencesStorage::class.java)

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

    /**
     * Checks if secure storage is available.
     *
     * Note: This method triggers lazy initialization of the encryption key,
     * which may involve key generation on first call. Subsequent calls are fast.
     */
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
        // Create a new Cipher instance for each operation to ensure thread safety
        val cipher = Cipher.getInstance(TRANSFORMATION)
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

        // Create a new Cipher instance for each operation to ensure thread safety
        val cipher = Cipher.getInstance(TRANSFORMATION)
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
            // Validate key length for AES-256 (32 bytes)
            if (keyBytes.size != KEY_SIZE / 8) {
                throw SecureStorageException(
                    "Invalid key size: expected ${KEY_SIZE / 8} bytes, got ${keyBytes.size} bytes"
                )
            }
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

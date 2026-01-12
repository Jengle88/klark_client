package ru.jengle88.klarkclient.data.security

/**
 * Platform-independent interface for storing sensitive data securely.
 *
 * Implementations use OS-specific secure storage mechanisms:
 * - Windows: Windows Credential Manager
 * - macOS: Keychain
 * - Linux: Secret Service (libsecret/gnome-keyring) with fallback
 */
interface SecureStorage {
    /**
     * Securely store a value associated with a key.
     *
     * @param key the identifier for the stored value
     * @param value the sensitive data to store
     * @throws SecureStorageException if storage fails
     */
    fun store(
        key: String,
        value: String,
    )

    /**
     * Retrieve a previously stored value.
     *
     * @param key the identifier for the stored value
     * @return the stored value, or null if not found
     * @throws SecureStorageException if retrieval fails
     */
    fun retrieve(key: String): String?

    /**
     * Remove a stored value.
     *
     * @param key the identifier for the stored value
     * @throws SecureStorageException if removal fails
     */
    fun remove(key: String)

    /**
     * Check if secure storage is available on this platform.
     *
     * @return true if secure storage is properly configured and available
     */
    fun isAvailable(): Boolean
}

/**
 * Exception thrown when secure storage operations fail.
 */
class SecureStorageException(message: String, cause: Throwable? = null) : Exception(message, cause)

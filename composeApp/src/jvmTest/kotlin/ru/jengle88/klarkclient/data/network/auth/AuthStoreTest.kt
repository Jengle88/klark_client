package ru.jengle88.klarkclient.data.network.auth

import java.util.prefs.Preferences
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull
import ru.jengle88.klarkclient.data.network.dto.UserProfile

class AuthStoreTest {
    private lateinit var authStore: AuthStoreImpl
    private lateinit var preferences: Preferences

    @BeforeTest
    fun setUp() {
        preferences = Preferences.userNodeForPackage(AuthStoreImpl::class.java)
        clearPreferences()
        authStore = AuthStoreImpl()
    }

    @AfterTest
    fun tearDown() {
        clearPreferences()
    }

    private fun clearPreferences() {
        preferences.remove(KEY_ACCESS_TOKEN)
        preferences.remove(KEY_REFRESH_TOKEN)
        preferences.remove(KEY_USER_PROFILE)
    }

    @Test
    fun `saveAuth updates StateFlow values`() {
        val token = "testAccessToken"
        val refreshToken = "testRefreshToken"
        val userProfile = UserProfile(firstName = "John", lastName = "Doe")

        authStore.saveAuth(token, refreshToken, userProfile)

        assertEquals(token, authStore.accessToken.value)
        assertEquals(refreshToken, authStore.refreshToken.value)
        assertEquals(userProfile, authStore.userProfile.value)
    }

    @Test
    fun `saveAuth persists data to Preferences`() {
        val token = "persistedAccessToken"
        val refreshToken = "persistedRefreshToken"
        val userProfile = UserProfile(firstName = "Jane", lastName = "Smith")

        authStore.saveAuth(token, refreshToken, userProfile)

        // Create a new instance to verify data was persisted
        val newAuthStore = AuthStoreImpl()

        assertEquals(token, newAuthStore.accessToken.value)
        assertEquals(refreshToken, newAuthStore.refreshToken.value)
        assertEquals(userProfile, newAuthStore.userProfile.value)
    }

    @Test
    fun `clearAuth removes all data from StateFlow`() {
        authStore.saveAuth("token", "refresh", UserProfile("First", "Last"))

        authStore.clearAuth()

        assertNull(authStore.accessToken.value)
        assertNull(authStore.refreshToken.value)
        assertNull(authStore.userProfile.value)
    }

    @Test
    fun `clearAuth removes all data from Preferences`() {
        authStore.saveAuth("token", "refresh", UserProfile("First", "Last"))

        authStore.clearAuth()

        // Create a new instance to verify data was removed from Preferences
        val newAuthStore = AuthStoreImpl()

        assertNull(newAuthStore.accessToken.value)
        assertNull(newAuthStore.refreshToken.value)
        assertNull(newAuthStore.userProfile.value)
    }

    @Test
    fun `loadUserProfile returns null when JSON deserialization fails`() {
        // Save invalid JSON directly to Preferences
        preferences.put(KEY_USER_PROFILE, "invalid json {{{")

        // Create a new instance to trigger loadUserProfile
        val newAuthStore = AuthStoreImpl()

        assertNull(newAuthStore.userProfile.value)
    }

    @Test
    fun `saveAuth with null refreshToken removes it from Preferences`() {
        // First save with a refresh token
        authStore.saveAuth("token", "refreshToken", null)
        assertEquals("refreshToken", authStore.refreshToken.value)

        // Now save without a refresh token
        authStore.saveAuth("token2", null, null)

        assertNull(authStore.refreshToken.value)

        // Verify it's also removed from Preferences
        val newAuthStore = AuthStoreImpl()
        assertNull(newAuthStore.refreshToken.value)
    }

    @Test
    fun `saveAuth with null userProfile removes it from Preferences`() {
        // First save with user profile
        authStore.saveAuth("token", null, UserProfile("First", "Last"))
        assertEquals(UserProfile("First", "Last"), authStore.userProfile.value)

        // Now save without a user profile
        authStore.saveAuth("token2", null, null)

        assertNull(authStore.userProfile.value)

        // Verify it's also removed from Preferences
        val newAuthStore = AuthStoreImpl()
        assertNull(newAuthStore.userProfile.value)
    }

    @Test
    fun `initial state is null when Preferences are empty`() {
        assertNull(authStore.accessToken.value)
        assertNull(authStore.refreshToken.value)
        assertNull(authStore.userProfile.value)
    }

    private companion object {
        private const val KEY_ACCESS_TOKEN = "auth_access_token"
        private const val KEY_REFRESH_TOKEN = "auth_refresh_token"
        private const val KEY_USER_PROFILE = "auth_user_profile"
    }
}

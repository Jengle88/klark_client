package ru.jengle88.klarkclient.domain.api.update

import io.github.z4kn4fein.semver.toVersionOrNull
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.get
import io.ktor.client.request.header
import kotlinx.coroutines.CancellationException

class UpdateChecker(
    private val client: HttpClient,
    private val owner: String,
    private val repo: String
) {
    suspend fun checkForUpdate(currentVersion: String): UpdateInfo? = try {
        val release: GithubRelease =
            client
                .get("https://api.github.com/repos/$owner/$repo/releases/latest") {
                    header("Accept", "application/vnd.github+json")
                    header("X-GitHub-Api-Version", "2022-11-28")
                    header("User-Agent", "Klark-Desktop-UpdateChecker")
                }.body()

        val latestVersion = release.tagName.lowercase().removePrefix(VERSION_PREFIX)
        val current = currentVersion.lowercase().removePrefix(VERSION_PREFIX)

        val latestSemver = latestVersion.toVersionOrNull()
        val currentSemver = current.toVersionOrNull()

        if (latestSemver != null && currentSemver != null && latestSemver > currentSemver) {
            UpdateInfo(
                version = latestVersion,
                downloadUrl = release.htmlUrl
            )
        } else {
            null
        }
    } catch (e: CancellationException) {
        throw e
    } catch (_: Exception) {
        // Covers network errors, HTTP errors, serialization errors, invalid versions, etc.
        null
    }

    private companion object {
        const val VERSION_PREFIX = "v"
    }
}

package ru.jengle88.klarkclient.domain.usecase.auth

import ru.jengle88.klarkclient.data.network.dto.UserProfile
import ru.jengle88.klarkclient.domain.api.auth.AuthStore
import ru.jengle88.klarkclient.ui.auth.AuthUserState

class GetAuthUserStateUseCase(
    private val authStore: AuthStore,
) {
    operator fun invoke(): AuthUserState =
        AuthUserState(
            isAuthorized = authStore.accessToken.value != null,
            initials = authStore.userProfile.value?.let { getUserNameInitials(it) },
        )

    private fun getUserNameInitials(profile: UserProfile): String {
        val firstInitial = profile.firstName.firstOrNull()
        val lastInitial = profile.lastName.firstOrNull()
        val initials =
            buildString {
                if (firstInitial != null) append(firstInitial)
                if (lastInitial != null) append(lastInitial)
            }
        return initials.uppercase().takeIf { it.isNotEmpty() } ?: "??"
    }
}

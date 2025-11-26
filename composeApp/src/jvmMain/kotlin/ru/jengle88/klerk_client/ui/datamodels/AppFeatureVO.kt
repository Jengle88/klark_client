package ru.jengle88.klerk_client.ui.datamodels

import androidx.compose.ui.graphics.vector.ImageVector

data class AppFeatureVO(
    val title: String,
    val description: String,
    val icon: ImageVector,
    val route: AppScreenDestination
)

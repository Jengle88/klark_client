package ru.jengle88.klarkclient.ui.datamodels

import androidx.compose.ui.graphics.vector.ImageVector

data class AppFeatureState(
    val title: String,
    val description: String,
    val icon: ImageVector,
    val route: AppScreenDestination,
)

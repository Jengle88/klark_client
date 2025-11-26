package ru.jengle88.klerk_client.ui.generatedocs

sealed class GenerateDocsEffect {
    data object ShowSuccessToast : GenerateDocsEffect()
    data class ShowErrorToast(val cause: String) : GenerateDocsEffect()
}
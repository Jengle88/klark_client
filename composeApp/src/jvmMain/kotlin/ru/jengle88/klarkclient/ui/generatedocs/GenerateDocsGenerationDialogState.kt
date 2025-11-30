package ru.jengle88.klarkclient.ui.generatedocs

import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.persistentListOf

data class GenerateDocsGenerationDialogState(
    val isGenerating: Boolean,
    val steps: ImmutableList<String>,
    val error: String? = null,
) {
    companion object {
        val EMPTY =
            GenerateDocsGenerationDialogState(
                isGenerating = false,
                steps = persistentListOf(),
                error = null,
            )
    }
}

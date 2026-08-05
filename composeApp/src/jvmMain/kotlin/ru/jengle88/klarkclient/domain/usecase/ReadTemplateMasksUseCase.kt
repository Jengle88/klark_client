package ru.jengle88.klarkclient.domain.usecase

import java.io.File
import ru.jengle88.klarkclient.domain.mapping.TemplateDataMapping

private const val DEFAULT_MASK_FILE_NAME = "маски.txt"

/**
 * Reads the mask list from `DEFAULT_MASK_FILE_NAME` inside a template folder.
 *
 * Masks are separated by `;` and trimmed. If the file is missing or unreadable,
 * an empty list is returned.
 */
class ReadTemplateMasksUseCase(private val templateDataMapping: TemplateDataMapping) {
    operator fun invoke(pathToTemplate: String, templateFolderName: String): List<String> {
        val masksFile = File(pathToTemplate, "$templateFolderName/$DEFAULT_MASK_FILE_NAME")
        if (!masksFile.exists() || !masksFile.isFile || !masksFile.canRead()) {
            return emptyList()
        }

        return templateDataMapping.parseMasks(masksFile.readText())
    }
}

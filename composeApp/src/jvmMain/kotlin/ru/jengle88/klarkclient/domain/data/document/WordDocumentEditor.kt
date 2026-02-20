package ru.jengle88.klarkclient.domain.data.document

import java.io.File

/**
 * Represents an editor for Word documents, providing functionality to save
 * the document to a file and replace occurrences of specific text within the document.
 */
interface WordDocumentEditor {
    /**
     * Saves the current Word document to the specified file.
     *
     * @param dstFile The file where the document will be saved.
     */
    fun saveToFile(dstFile: File)

    /**
     * Replaces all occurrences of the specified old text with the new text within the document.
     *
     * @param oldText The text to be replaced within the document.
     * @param newText The text to replace the old text with.
     */
    fun replaceTextInDocument(
        oldText: String,
        newText: String,
    )
}
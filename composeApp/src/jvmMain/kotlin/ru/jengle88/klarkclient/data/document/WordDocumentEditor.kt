package ru.jengle88.klarkclient.data.document

import java.io.File

interface WordDocumentEditor {
    fun saveToFile(file: File)
    fun replaceTextInDocument(
        oldText: String,
        newText: String,
    )
}
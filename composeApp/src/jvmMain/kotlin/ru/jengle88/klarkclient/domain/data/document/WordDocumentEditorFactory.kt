package ru.jengle88.klarkclient.domain.data.document

import java.io.File

interface WordDocumentEditorFactory {
    fun create(docFile: File): WordDocumentEditor?
}
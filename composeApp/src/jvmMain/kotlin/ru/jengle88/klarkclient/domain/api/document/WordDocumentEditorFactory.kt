package ru.jengle88.klarkclient.domain.api.document

import java.io.File

interface WordDocumentEditorFactory {
    fun create(docFile: File): WordDocumentEditor?
}
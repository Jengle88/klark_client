package ru.jengle88.klarkclient.data.document

import ru.jengle88.klarkclient.domain.api.document.WordDocumentEditor
import ru.jengle88.klarkclient.domain.api.document.WordDocumentEditorFactory
import java.io.File

class WordDocumentEditorFactoryImpl : WordDocumentEditorFactory {
    override fun create(docFile: File): WordDocumentEditor? =
        when (docFile.extension) {
            "docx" -> WordDocumentEditorDocxImpl.createEditor(docFile)
            else -> null
        }
}

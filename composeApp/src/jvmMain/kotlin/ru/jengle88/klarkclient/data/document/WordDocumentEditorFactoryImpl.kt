package ru.jengle88.klarkclient.data.document

import java.io.File
import ru.jengle88.klarkclient.domain.api.document.WordDocumentEditor
import ru.jengle88.klarkclient.domain.api.document.WordDocumentEditorFactory

class WordDocumentEditorFactoryImpl : WordDocumentEditorFactory {
    override fun create(docFile: File): WordDocumentEditor? = when (docFile.extension) {
        "docx" -> WordDocumentEditorDocxImpl.createEditor(docFile)
        else -> null
    }
}

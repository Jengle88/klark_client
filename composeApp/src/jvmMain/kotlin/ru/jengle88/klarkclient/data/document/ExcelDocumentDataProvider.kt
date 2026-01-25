package ru.jengle88.klarkclient.data.document

import java.io.File

interface ExcelDocumentDataProvider {
    fun readData(
        table: File,
        ignoreLastNColumn: Int,
        unionLastNColumn: Int,
    ): List<List<String>>
}

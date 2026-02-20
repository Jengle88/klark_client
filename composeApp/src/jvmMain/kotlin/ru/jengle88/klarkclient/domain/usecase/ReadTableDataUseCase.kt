package ru.jengle88.klarkclient.domain.usecase

import ru.jengle88.klarkclient.domain.data.document.ExcelDocumentDataProvider
import java.io.File

class ReadTableDataUseCase(
    private val excelDocumentDataProvider: ExcelDocumentDataProvider
) {

    operator fun invoke(pathToTable: String, ignoreLastNColumn: Int? = null, unionLastNColumn: Int? = null): List<List<String>> {
        return excelDocumentDataProvider.readData(
            File(pathToTable),
            ignoreLastNColumn ?: 0,
            unionLastNColumn ?: 0,
        )
    }
}
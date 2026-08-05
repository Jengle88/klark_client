package ru.jengle88.klarkclient.domain.usecase

import ru.jengle88.klarkclient.data.document.TableConfiguration
import ru.jengle88.klarkclient.domain.api.document.ExcelDocumentDataProvider

class ReadTableDataUseCase(private val excelDocumentDataProvider: ExcelDocumentDataProvider) {
    operator fun invoke(
        pathToTable: String,
        ignoreLastNColumn: Int? = null,
        unionLastNColumn: Int? = null,
    ): List<List<String>> = excelDocumentDataProvider
        .readData(
            TableConfiguration(
                file = java.io.File(pathToTable),
                ignoreLastNColumn = ignoreLastNColumn ?: 0,
                unionLastNColumn = unionLastNColumn ?: 0,
            ),
        ).rows
}

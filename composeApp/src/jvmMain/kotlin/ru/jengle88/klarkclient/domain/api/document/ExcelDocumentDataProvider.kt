package ru.jengle88.klarkclient.domain.api.document

import ru.jengle88.klarkclient.data.document.TableConfiguration
import ru.jengle88.klarkclient.data.document.TableContent

/**
 * Provides functionality to read data from Excel documents such as XLSX files and process the content.
 */
interface ExcelDocumentDataProvider {
    /**
     * Reads data from an Excel table file based on the provided configuration.
     *
     * @param config The configuration specifying the table file, optional column and row ranges,
     *               as well as adjustments for columns to ignore or merge.
     * @return The content of the table, represented as a list of rows, where each row is a list of strings.
     */
    fun readData(
        config: TableConfiguration
    ): TableContent
}

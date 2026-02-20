package ru.jengle88.klarkclient.domain.data.document

import java.io.File

/**
 * Provides functionality to read data from Excel documents such as XLSX files and process the content.
 */
interface ExcelDocumentDataProvider {
    /**
     * Reads data from the specified table file (for example, XLSX) and processes the content based on the provided parameters.
     *
     * @param table The file representing the table to be read.
     * @param ignoreLastNColumn The number of columns from the end of the table to be ignored during data processing.
     * @param unionLastNColumn The number of columns from the end of the table to be merged into a single column.
     * @return A list of lists, where each inner list represents a row of processed data from the table.
     */
    fun readData(
        table: File,
        ignoreLastNColumn: Int,
        unionLastNColumn: Int,
    ): List<List<String>>
}

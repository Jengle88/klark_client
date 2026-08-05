package ru.jengle88.klarkclient.data.document

import java.io.FileInputStream
import java.text.SimpleDateFormat
import java.util.*
import kotlin.math.min
import org.apache.poi.ss.usermodel.Cell
import org.apache.poi.ss.usermodel.CellType
import org.apache.poi.ss.usermodel.DateUtil
import org.apache.poi.ss.usermodel.Row
import org.apache.poi.xssf.usermodel.XSSFWorkbook
import ru.jengle88.klarkclient.common.padLast
import ru.jengle88.klarkclient.domain.api.document.ExcelDocumentDataProvider

class ExcelDocumentDataProviderXlsxImpl(private val locale: Locale = Locale.getDefault()) :
    ExcelDocumentDataProvider {
    private val dateFormat = SimpleDateFormat("dd.MM.yyyy")

    override fun readData(config: TableConfiguration): TableContent {
        check(config.ignoreLastNColumn >= 0)
        check(config.unionLastNColumn >= 0)

        val table = config.file
        if (!table.exists() || table.extension != "xlsx") {
            return TableContent(emptyList())
        }

        val resultRows =
            try {
                FileInputStream(table).use { fis ->
                    XSSFWorkbook(fis).use { workbook ->
                        buildList<List<String>> {
                            for (sheet in workbook) {
                                for (row in sheet) {
                                    add(parseRow(row))
                                }
                            }
                        }
                    }
                }
            } catch (e: Exception) {
                throw e
            }

        val maxRowLength = resultRows.maxOfOrNull { it.size } ?: 0
        val finalRows = resultRows.map {
            // pad + drop
            val padAndDrop = it.padLast(maxRowLength, "").dropLast(config.ignoreLastNColumn)
            // union
            val lastValues = padAndDrop.takeLast(config.unionLastNColumn).joinToString(" ")
            padAndDrop
                .dropLast(config.unionLastNColumn)
                .toMutableList()
                .apply {
                    if (config.unionLastNColumn > 0) {
                        add(lastValues)
                    }
                }
                .dropLastWhile { data -> data.isEmpty() }
        }

        return TableContent(finalRows)
    }

    private fun parseRow(row: Row?): List<String> {
        val rowData = mutableListOf<String>()
        if (row == null) return rowData

        val lastCellNum = row.lastCellNum
        val columnsToRead = min(lastCellNum.toInt(), COLUMNS_LIMIT)

        for (i in 0 until columnsToRead) {
            val cell = row.getCell(i, Row.MissingCellPolicy.RETURN_BLANK_AS_NULL)
            val cellValue = cell?.let { parseCell(it) } ?: ""
            rowData.add(cellValue)
        }

        return rowData
    }

    private fun parseCell(cell: Cell): String {
        val cellValue =
            when (cell.cellType) {
                CellType.STRING -> cell.stringCellValue
                CellType.NUMERIC -> {
                    if (DateUtil.isCellDateFormatted(cell)) {
                        dateFormat.format(cell.dateCellValue)
                    } else {
                        val numericValue = cell.numericCellValue
                        if (numericValue % 1 == 0.0) {
                            numericValue.toBigDecimal().toBigInteger().toString()
                        } else {
                            String.format(locale, "%.2f", numericValue)
                        }
                    }
                }

                CellType.BOOLEAN -> cell.booleanCellValue.toString()
                CellType.FORMULA -> {
                    try {
                        cell.stringCellValue
                    } catch (_: Exception) {
                        try {
                            val numericValue = cell.numericCellValue
                            if (numericValue % 1 == 0.0) {
                                numericValue.toBigDecimal().toBigInteger().toString()
                            } else {
                                String.format(locale, "%.2f", numericValue)
                            }
                        } catch (_: Exception) {
                            cell.cellFormula
                        }
                    }
                }

                CellType.BLANK -> ""
                else -> ""
            }
        return cellValue ?: ""
    }

    private companion object Companion {
        private const val COLUMNS_LIMIT = 100
    }
}

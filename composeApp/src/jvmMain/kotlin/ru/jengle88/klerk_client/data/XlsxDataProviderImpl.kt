package ru.jengle88.klerk_client.data

import org.apache.poi.ss.usermodel.Cell
import org.apache.poi.ss.usermodel.CellType
import org.apache.poi.ss.usermodel.DateUtil
import org.apache.poi.ss.usermodel.Row
import org.apache.poi.xssf.usermodel.XSSFWorkbook
import java.io.File
import java.io.FileInputStream
import java.text.SimpleDateFormat

class XlsxDataProviderImpl : XlsxDataProvider {

    private val dateFormat = SimpleDateFormat("dd.MM.yyyy")

    override fun readData(path: String): List<List<String>> {
        val file = File(path)
        if (!file.exists() || file.extension != "xlsx") {
            return emptyList()
        }
        return buildList {
            FileInputStream(file).use { fis ->
                XSSFWorkbook(fis).use { workbook ->
                    workbook.sheetIterator().forEach { sheet ->
                        for (row in sheet) {
                            add(parseRow(row))
                        }
                    }
                }
            }
        }
    }

    private fun parseRow(row: Row?): List<String> {
        val rowData = mutableListOf<String>()

        for (cell in row?.take(100) ?: return rowData) {
            val cellValue = cell?.let { parseCell(cell) } ?: continue

            if (cellValue.isEmpty()) continue
            rowData.add(cellValue)
        }
        return rowData
    }

    private fun parseCell(cell: Cell): String {
        val cellValue = when (cell.cellType) {
            CellType.STRING -> cell.stringCellValue
            CellType.NUMERIC -> {
                if (DateUtil.isCellDateFormatted(cell)) {
                    dateFormat.format(cell.dateCellValue)
                } else {
                    val numericValue = cell.numericCellValue
                    if (numericValue % 1 == 0.0) {
                        numericValue.toBigDecimal().toBigInteger().toString()
                    } else {
                        String.format("%.2f", numericValue)
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
                            String.format("%.2f", numericValue)
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
}
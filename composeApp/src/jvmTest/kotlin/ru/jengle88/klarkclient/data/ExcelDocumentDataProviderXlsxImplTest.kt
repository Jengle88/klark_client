package ru.jengle88.klarkclient.data

import java.io.File
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue
import org.apache.poi.ss.usermodel.CellType
import org.apache.poi.xssf.usermodel.XSSFWorkbook
import ru.jengle88.klarkclient.data.document.ExcelDocumentDataProviderXlsxImpl
import ru.jengle88.klarkclient.data.document.TableConfiguration

class ExcelDocumentDataProviderXlsxImplTest {
    private lateinit var provider: ExcelDocumentDataProviderXlsxImpl
    private lateinit var tempDir: File
    private val dateFormat = SimpleDateFormat("dd.MM.yyyy")

    @BeforeTest
    fun setUp() {
        provider = ExcelDocumentDataProviderXlsxImpl(Locale.US)
        tempDir =
            File(System.getProperty("java.io.tmpdir"), "xlsx_test_${System.currentTimeMillis()}")
        tempDir.mkdirs()
    }

    @AfterTest
    fun tearDown() {
        tempDir.deleteRecursively()
    }

    @Test
    fun `readData returns empty list for non-existent file`() {
        val result = provider.readData(
            TableConfiguration(
                File("/non/existent/path.xlsx"),
                ignoreLastNColumn = 0,
                unionLastNColumn = 0
            )
        )
        assertTrue(result.rows.isEmpty())
    }

    @Test
    fun `readData returns empty list for non-xlsx file`() {
        val textFile = File(tempDir, "test.txt")
        textFile.writeText("not an xlsx file")

        val result = provider.readData(
            TableConfiguration(textFile, ignoreLastNColumn = 0, unionLastNColumn = 0)
        )
        assertTrue(result.rows.isEmpty())
    }

    @Test
    fun `readData reads string cells correctly`() {
        val xlsxFile =
            createTestXlsx("string_test.xlsx") { workbook ->
                val sheet = workbook.createSheet("Test")
                val row = sheet.createRow(0)
                row.createCell(0).setCellValue("Hello")
                row.createCell(1).setCellValue("World")
            }

        val result = provider.readData(
            TableConfiguration(xlsxFile, ignoreLastNColumn = 0, unionLastNColumn = 0)
        )

        assertEquals(1, result.rows.size)
        assertEquals(listOf("Hello", "World"), result.rows[0])
    }

    @Test
    fun `readData reads integer numeric cells correctly`() {
        val xlsxFile =
            createTestXlsx("integer_test.xlsx") { workbook ->
                val sheet = workbook.createSheet("Test")
                val row = sheet.createRow(0)
                row.createCell(0).setCellValue(42.0)
                row.createCell(1).setCellValue(100.0)
            }

        val result = provider.readData(
            TableConfiguration(xlsxFile, ignoreLastNColumn = 0, unionLastNColumn = 0)
        )

        assertEquals(1, result.rows.size)
        assertEquals(listOf("42", "100"), result.rows[0])
    }

    @Test
    fun `readData reads decimal numeric cells correctly`() {
        val xlsxFile =
            createTestXlsx("decimal_test.xlsx") { workbook ->
                val sheet = workbook.createSheet("Test")
                val row = sheet.createRow(0)
                row.createCell(0).setCellValue(3.14159)
                row.createCell(1).setCellValue(2.5)
            }

        val result = provider.readData(
            TableConfiguration(xlsxFile, ignoreLastNColumn = 0, unionLastNColumn = 0)
        )

        assertEquals(1, result.rows.size)
        assertEquals(listOf("3.14", "2.50"), result.rows[0])
    }

    @Test
    fun `readData reads date cells correctly`() {
        val testDate = Date()
        val xlsxFile =
            createTestXlsx("date_test.xlsx") { workbook ->
                val sheet = workbook.createSheet("Test")
                val row = sheet.createRow(0)
                val cell = row.createCell(0)
                cell.setCellValue(testDate)
                val dateStyle = workbook.createCellStyle()
                dateStyle.dataFormat = workbook.createDataFormat().getFormat("dd/mm/yyyy")
                cell.cellStyle = dateStyle
            }

        val result = provider.readData(
            TableConfiguration(xlsxFile, ignoreLastNColumn = 0, unionLastNColumn = 0)
        )

        assertEquals(1, result.rows.size)
        assertEquals(1, result.rows[0].size)
        assertEquals(dateFormat.format(testDate), result.rows[0][0])
    }

    @Test
    fun `readData reads boolean cells correctly`() {
        val xlsxFile =
            createTestXlsx("boolean_test.xlsx") { workbook ->
                val sheet = workbook.createSheet("Test")
                val row = sheet.createRow(0)
                row.createCell(0).setCellValue(true)
                row.createCell(1).setCellValue(false)
            }

        val result = provider.readData(
            TableConfiguration(xlsxFile, ignoreLastNColumn = 0, unionLastNColumn = 0)
        )

        assertEquals(1, result.rows.size)
        assertEquals(listOf("true", "false"), result.rows[0])
    }

    @Test
    fun `readData preserves blank cells`() {
        val xlsxFile =
            createTestXlsx("blank_test.xlsx") { workbook ->
                val sheet = workbook.createSheet("Test")
                val row = sheet.createRow(0)
                row.createCell(0).setCellValue("First")
                row.createCell(1).setCellType(CellType.BLANK)
                row.createCell(2).setCellValue("Third")
            }

        val result = provider.readData(
            TableConfiguration(xlsxFile, ignoreLastNColumn = 0, unionLastNColumn = 0)
        )

        assertEquals(1, result.rows.size)
        assertEquals(listOf("First", "", "Third"), result.rows[0])
    }

    @Test
    fun `readData preserves empty cells and alignment`() {
        val xlsxFile =
            createTestXlsx("alignment_test.xlsx") { workbook ->
                val sheet = workbook.createSheet("Test")
                val row = sheet.createRow(0)
                row.createCell(0).setCellValue("A")
                // Cell 1 is missing/null
                row.createCell(2).setCellValue("C")
                row.createCell(3).setCellType(CellType.BLANK) // Explicitly blank
                row.createCell(4).setCellValue("E")
            }

        val result = provider.readData(
            TableConfiguration(xlsxFile, ignoreLastNColumn = 0, unionLastNColumn = 0)
        )

        assertEquals(1, result.rows.size)
        // Expectation: "A", "", "C", "", "E"
        val rowData = result.rows[0]
        assertEquals(5, rowData.size)
        assertEquals("A", rowData[0])
        assertEquals("", rowData[1])
        assertEquals("C", rowData[2])
        assertEquals("", rowData[3])
        assertEquals("E", rowData[4])
    }

    @Test
    fun `readData remove trailing empty cells`() {
        val xlsxFile =
            createTestXlsx("trailing_empty_test.xlsx") { workbook ->
                val sheet = workbook.createSheet("Test")
                val row = sheet.createRow(0)
                row.createCell(0).setCellValue("A")
                row.createCell(1).setCellType(CellType.BLANK)
                row.createCell(2).setCellType(CellType.BLANK)
            }

        val result = provider.readData(
            TableConfiguration(xlsxFile, ignoreLastNColumn = 0, unionLastNColumn = 0)
        )

        assertEquals(1, result.rows.size)
        assertEquals(1, result.rows[0].size)
        assertEquals("A", result.rows[0][0])
    }

    @Test
    fun `readData reads formula cells with string result`() {
        val xlsxFile =
            createTestXlsx("formula_string_test.xlsx") { workbook ->
                val sheet = workbook.createSheet("Test")
                val row = sheet.createRow(0)
                row.createCell(0).setCellValue("Hello")
                val formulaCell = row.createCell(1)
                formulaCell.cellFormula = "CONCATENATE(A1, \" World\")"
            }

        val result = provider.readData(
            TableConfiguration(xlsxFile, ignoreLastNColumn = 0, unionLastNColumn = 0)
        )

        assertEquals(1, result.rows.size)
        assertTrue(result.rows[0].isNotEmpty())
    }

    @Test
    fun `readData reads formula cells with numeric result`() {
        val xlsxFile =
            createTestXlsx("formula_numeric_test.xlsx") { workbook ->
                val sheet = workbook.createSheet("Test")
                val row = sheet.createRow(0)
                row.createCell(0).setCellValue(10.0)
                row.createCell(1).setCellValue(20.0)
                val formulaCell = row.createCell(2)
                formulaCell.cellFormula = "A1+B1"
            }

        val result = provider.readData(
            TableConfiguration(xlsxFile, ignoreLastNColumn = 0, unionLastNColumn = 0)
        )

        assertEquals(1, result.rows.size)
        assertTrue(result.rows[0].contains("10"))
        assertTrue(result.rows[0].contains("20"))
    }

    @Test
    fun `readData handles empty xlsx file`() {
        val xlsxFile =
            createTestXlsx("empty_test.xlsx") { workbook ->
                workbook.createSheet("Empty")
            }

        val result = provider.readData(
            TableConfiguration(xlsxFile, ignoreLastNColumn = 0, unionLastNColumn = 0)
        )

        assertTrue(result.rows.isEmpty())
    }

    @Test
    fun `readData reads multiple rows correctly`() {
        val xlsxFile =
            createTestXlsx("multi_row_test.xlsx") { workbook ->
                val sheet = workbook.createSheet("Test")
                for (i in 0..2) {
                    val row = sheet.createRow(i)
                    row.createCell(0).setCellValue("Row$i")
                    row.createCell(1).setCellValue((i + 1).toDouble())
                }
            }

        val result = provider.readData(
            TableConfiguration(xlsxFile, ignoreLastNColumn = 0, unionLastNColumn = 0)
        )

        assertEquals(3, result.rows.size)
        assertEquals(listOf("Row0", "1"), result.rows[0])
        assertEquals(listOf("Row1", "2"), result.rows[1])
        assertEquals(listOf("Row2", "3"), result.rows[2])
    }

    @Test
    fun `readData reads multiple sheets correctly`() {
        val xlsxFile =
            createTestXlsx("multi_sheet_test.xlsx") { workbook ->
                val sheet1 = workbook.createSheet("Sheet1")
                sheet1.createRow(0).createCell(0).setCellValue("Sheet1Data")

                val sheet2 = workbook.createSheet("Sheet2")
                sheet2.createRow(0).createCell(0).setCellValue("Sheet2Data")
            }

        val result = provider.readData(
            TableConfiguration(xlsxFile, ignoreLastNColumn = 0, unionLastNColumn = 0)
        )

        assertEquals(2, result.rows.size)
        assertEquals(listOf("Sheet1Data"), result.rows[0])
        assertEquals(listOf("Sheet2Data"), result.rows[1])
    }

    @Test
    fun `readData handles mixed cell types in same row`() {
        val testDate = Date()
        val xlsxFile =
            createTestXlsx("mixed_test.xlsx") { workbook ->
                val sheet = workbook.createSheet("Test")
                val row = sheet.createRow(0)
                row.createCell(0).setCellValue("Text")
                row.createCell(1).setCellValue(42.0)
                row.createCell(2).setCellValue(true)

                val dateCell = row.createCell(3)
                dateCell.setCellValue(testDate)
                val dateStyle = workbook.createCellStyle()
                dateStyle.dataFormat = workbook.createDataFormat().getFormat("dd/mm/yyyy")
                dateCell.cellStyle = dateStyle
            }

        val result = provider.readData(
            TableConfiguration(xlsxFile, ignoreLastNColumn = 0, unionLastNColumn = 0)
        )

        assertEquals(1, result.rows.size)
        assertEquals(4, result.rows[0].size)
        assertEquals("Text", result.rows[0][0])
        assertEquals("42", result.rows[0][1])
        assertEquals("true", result.rows[0][2])
        assertEquals(dateFormat.format(testDate), result.rows[0][3])
    }

    @Test
    fun `readData handles large numbers correctly`() {
        val xlsxFile =
            createTestXlsx("large_number_test.xlsx") { workbook ->
                val sheet = workbook.createSheet("Test")
                val row = sheet.createRow(0)
                row.createCell(0).setCellValue(1000000.0)
                row.createCell(1).setCellValue(999999999.0)
            }

        val result = provider.readData(
            TableConfiguration(xlsxFile, ignoreLastNColumn = 0, unionLastNColumn = 0)
        )

        assertEquals(1, result.rows.size)
        assertEquals(listOf("1000000", "999999999"), result.rows[0])
    }

    @Test
    fun `readData handles negative numbers correctly`() {
        val xlsxFile =
            createTestXlsx("negative_number_test.xlsx") { workbook ->
                val sheet = workbook.createSheet("Test")
                val row = sheet.createRow(0)
                row.createCell(0).setCellValue(-42.0)
                row.createCell(1).setCellValue(-3.14)
            }

        val result = provider.readData(
            TableConfiguration(xlsxFile, ignoreLastNColumn = 0, unionLastNColumn = 0)
        )

        assertEquals(1, result.rows.size)
        assertEquals("-42", result.rows[0][0])
        assertEquals("-3.14", result.rows[0][1])
    }

    @Test
    fun `readData handles zero correctly`() {
        val xlsxFile =
            createTestXlsx("zero_test.xlsx") { workbook ->
                val sheet = workbook.createSheet("Test")
                val row = sheet.createRow(0)
                row.createCell(0).setCellValue(0.0)
            }

        val result = provider.readData(
            TableConfiguration(xlsxFile, ignoreLastNColumn = 0, unionLastNColumn = 0)
        )

        assertEquals(1, result.rows.size)
        assertEquals(listOf("0"), result.rows[0])
    }

    @Test
    fun `readData ignores last N columns correctly`() {
        val xlsxFile =
            createTestXlsx("ignore_columns_test.xlsx") { workbook ->
                val sheet = workbook.createSheet("Test")
                val row = sheet.createRow(0)
                row.createCell(0).setCellValue("A")
                row.createCell(1).setCellValue("B")
                row.createCell(2).setCellValue("C")
                row.createCell(3).setCellValue("D")
            }

        val result = provider.readData(
            TableConfiguration(xlsxFile, ignoreLastNColumn = 2, unionLastNColumn = 0)
        )

        assertEquals(1, result.rows.size)
        assertEquals(listOf("A", "B"), result.rows[0])
    }

    @Test
    fun `readData ignores all columns when ignoreLastNColumn equals row size`() {
        val xlsxFile =
            createTestXlsx("ignore_all_columns_test.xlsx") { workbook ->
                val sheet = workbook.createSheet("Test")
                val row = sheet.createRow(0)
                row.createCell(0).setCellValue("A")
                row.createCell(1).setCellValue("B")
            }

        val result = provider.readData(
            TableConfiguration(xlsxFile, ignoreLastNColumn = 2, unionLastNColumn = 0)
        )

        assertEquals(1, result.rows.size)
        assertTrue(result.rows[0].isEmpty())
    }

    @Test
    fun `readData unions last N columns correctly`() {
        val xlsxFile =
            createTestXlsx("union_columns_test.xlsx") { workbook ->
                val sheet = workbook.createSheet("Test")
                val row = sheet.createRow(0)
                row.createCell(0).setCellValue("First")
                row.createCell(1).setCellValue("Second")
                row.createCell(2).setCellValue("Third")
                row.createCell(3).setCellValue("Fourth")
            }

        val result = provider.readData(
            TableConfiguration(xlsxFile, ignoreLastNColumn = 0, unionLastNColumn = 2)
        )

        assertEquals(1, result.rows.size)
        assertEquals(listOf("First", "Second", "Third Fourth"), result.rows[0])
    }

    @Test
    fun `readData unions all columns when unionLastNColumn equals row size`() {
        val xlsxFile =
            createTestXlsx("union_all_columns_test.xlsx") { workbook ->
                val sheet = workbook.createSheet("Test")
                val row = sheet.createRow(0)
                row.createCell(0).setCellValue("A")
                row.createCell(1).setCellValue("B")
                row.createCell(2).setCellValue("C")
            }

        val result = provider.readData(
            TableConfiguration(xlsxFile, ignoreLastNColumn = 0, unionLastNColumn = 3)
        )

        assertEquals(1, result.rows.size)
        assertEquals(listOf("A B C"), result.rows[0])
    }

    @Test
    fun `readData combines ignoreLastNColumn and unionLastNColumn correctly`() {
        val xlsxFile =
            createTestXlsx("combine_test.xlsx") { workbook ->
                val sheet = workbook.createSheet("Test")
                val row = sheet.createRow(0)
                row.createCell(0).setCellValue("Keep1")
                row.createCell(1).setCellValue("Keep2")
                row.createCell(2).setCellValue("Union1")
                row.createCell(3).setCellValue("Union2")
                row.createCell(4).setCellValue("Ignore1")
                row.createCell(5).setCellValue("Ignore2")
            }

        val result = provider.readData(
            TableConfiguration(xlsxFile, ignoreLastNColumn = 2, unionLastNColumn = 2)
        )

        assertEquals(1, result.rows.size)
        assertEquals(listOf("Keep1", "Keep2", "Union1 Union2"), result.rows[0])
    }

    @Test
    fun `readData handles multiple rows with ignoreLastNColumn`() {
        val xlsxFile =
            createTestXlsx("multi_row_ignore_test.xlsx") { workbook ->
                val sheet = workbook.createSheet("Test")
                for (i in 0..1) {
                    val row = sheet.createRow(i)
                    row.createCell(0).setCellValue("Row${i}A")
                    row.createCell(1).setCellValue("Row${i}B")
                    row.createCell(2).setCellValue("Row${i}C")
                }
            }

        val result = provider.readData(
            TableConfiguration(xlsxFile, ignoreLastNColumn = 1, unionLastNColumn = 0)
        )

        assertEquals(2, result.rows.size)
        assertEquals(listOf("Row0A", "Row0B"), result.rows[0])
        assertEquals(listOf("Row1A", "Row1B"), result.rows[1])
    }

    @Test
    fun `readData handles multiple rows with unionLastNColumn`() {
        val xlsxFile =
            createTestXlsx("multi_row_union_test.xlsx") { workbook ->
                val sheet = workbook.createSheet("Test")
                for (i in 0..1) {
                    val row = sheet.createRow(i)
                    row.createCell(0).setCellValue("Row${i}A")
                    row.createCell(1).setCellValue("Row${i}B")
                    row.createCell(2).setCellValue("Row${i}C")
                }
            }

        val result = provider.readData(
            TableConfiguration(xlsxFile, ignoreLastNColumn = 0, unionLastNColumn = 2)
        )

        assertEquals(2, result.rows.size)
        assertEquals(listOf("Row0A", "Row0B Row0C"), result.rows[0])
        assertEquals(listOf("Row1A", "Row1B Row1C"), result.rows[1])
    }

    @Test
    fun `readData pads rows to same length before applying ignoreLastNColumn`() {
        val xlsxFile =
            createTestXlsx("uneven_rows_ignore_test.xlsx") { workbook ->
                val sheet = workbook.createSheet("Test")
                val row1 = sheet.createRow(0)
                row1.createCell(0).setCellValue("A1")
                row1.createCell(1).setCellValue("B1")
                row1.createCell(2).setCellValue("C1")
                row1.createCell(3).setCellValue("D1")

                val row2 = sheet.createRow(1)
                row2.createCell(0).setCellValue("A2")
                row2.createCell(1).setCellValue("B2")
            }

        val result = provider.readData(
            TableConfiguration(xlsxFile, ignoreLastNColumn = 2, unionLastNColumn = 0)
        )

        assertEquals(2, result.rows.size)
        assertEquals(listOf("A1", "B1"), result.rows[0])
        assertEquals(listOf("A2", "B2"), result.rows[1])
    }

    @Test
    fun `readData with zero ignoreLastNColumn and zero unionLastNColumn returns original data`() {
        val xlsxFile =
            createTestXlsx("no_modification_test.xlsx") { workbook ->
                val sheet = workbook.createSheet("Test")
                val row = sheet.createRow(0)
                row.createCell(0).setCellValue("A")
                row.createCell(1).setCellValue("B")
                row.createCell(2).setCellValue("C")
            }

        val result = provider.readData(
            TableConfiguration(xlsxFile, ignoreLastNColumn = 0, unionLastNColumn = 0)
        )

        assertEquals(1, result.rows.size)
        assertEquals(listOf("A", "B", "C"), result.rows[0])
    }

    @Test
    fun `readDataFromRange reads exact boundaries correctly`() {
        val xlsxFile =
            createTestXlsx("range_test.xlsx") { workbook ->
                val sheet = workbook.createSheet("Test")
                for (r in 0..4) {
                    val row = sheet.createRow(r)
                    for (c in 0..4) {
                        row.createCell(c).setCellValue("R${r}C$c")
                    }
                }
            }

        val result = provider.readData(TableConfiguration(xlsxFile, xRange = 1..3, yRange = 2..4))

        assertEquals(3, result.rows.size)
        assertEquals(listOf("R1C2", "R1C3", "R1C4"), result.rows[0])
        assertEquals(listOf("R2C2", "R2C3", "R2C4"), result.rows[1])
        assertEquals(listOf("R3C2", "R3C3", "R3C4"), result.rows[2])
    }

    @Test
    fun `readDataFromRange preserves empty cells within range`() {
        val xlsxFile =
            createTestXlsx("range_empty_cells_test.xlsx") { workbook ->
                val sheet = workbook.createSheet("Test")
                val row1 = sheet.createRow(0)
                row1.createCell(0).setCellValue("A")
                row1.createCell(2).setCellValue("C")

                val row2 = sheet.createRow(1)
                row2.createCell(0).setCellValue("D")
                row2.createCell(1).setCellValue("E")
            }

        val result = provider.readData(TableConfiguration(xlsxFile, xRange = 0..1, yRange = 0..2))

        assertEquals(2, result.rows.size)
        assertEquals(listOf("A", "", "C"), result.rows[0])
        assertEquals(listOf("D", "E", ""), result.rows[1])
    }

    @Test
    fun `readDataFromRange handles out of bounds ranges correctly`() {
        val xlsxFile =
            createTestXlsx("range_out_of_bounds.xlsx") { workbook ->
                val sheet = workbook.createSheet("Test")
                val row = sheet.createRow(0)
                row.createCell(0).setCellValue("A")
            }

        // Запрашиваем строки 0..2 и колонки 0..2, хотя в файле заполнена только ячейка (0,0)
        val result = provider.readData(TableConfiguration(xlsxFile, xRange = 0..2, yRange = 0..2))

        assertEquals(3, result.rows.size)
        assertEquals(listOf("A", "", ""), result.rows[0])
        assertEquals(listOf("", "", ""), result.rows[1])
        assertEquals(listOf("", "", ""), result.rows[2])
    }

    @Test
    fun `readDataFromRange returns empty list for non-existent file`() {
        val result = provider.readData(
            TableConfiguration(
                File("/non/existent/path.xlsx"),
                xRange =
                0..1,
                yRange = 0..1
            )
        )
        assertTrue(result.rows.isEmpty())
    }

    @Test
    fun `readDataFromRange reads typical populated table correctly`() {
        val xlsxFile =
            createTestXlsx("populated_table_test.xlsx") { workbook ->
                val sheet = workbook.createSheet("Data")
                val headers = sheet.createRow(0)
                headers.createCell(0).setCellValue("Name")
                headers.createCell(1).setCellValue("Age")
                headers.createCell(2).setCellValue("Status")

                val row1 = sheet.createRow(1)
                row1.createCell(0).setCellValue("John")
                row1.createCell(1).setCellValue(30.0)
                row1.createCell(2).setCellValue(true)

                val row2 = sheet.createRow(2)
                row2.createCell(0).setCellValue("Alice")
                row2.createCell(1).setCellValue(25.0)
                row2.createCell(2).setCellValue(false)
            }

        val result = provider.readData(TableConfiguration(xlsxFile, xRange = 0..2, yRange = 0..2))

        assertEquals(3, result.rows.size)
        assertEquals(listOf("Name", "Age", "Status"), result.rows[0])
        assertEquals(listOf("John", "30", "true"), result.rows[1])
        assertEquals(listOf("Alice", "25", "false"), result.rows[2])
    }

    private fun createTestXlsx(fileName: String, configure: (XSSFWorkbook) -> Unit): File {
        val file = File(tempDir, fileName)
        XSSFWorkbook().use { workbook ->
            configure(workbook)
            FileOutputStream(file).use { fos ->
                workbook.write(fos)
            }
        }
        return file
    }
}

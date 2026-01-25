package ru.jengle88.klarkclient.data

import org.apache.poi.xwpf.usermodel.XWPFDocument
import ru.jengle88.klarkclient.data.document.WordDocumentEditorDocxImpl
import java.io.File
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class WordDocumentEditorTest {
    private lateinit var tempDir: File

    @BeforeTest
    fun setUp() {
        tempDir = File(System.getProperty("java.io.tmpdir"), "word_test_${System.currentTimeMillis()}")
        tempDir.mkdirs()
    }

    @AfterTest
    fun tearDown() {
        tempDir.deleteRecursively()
    }

    @Test
    fun `replaceTextInDocument replaces text in simple paragraph`() {
        val docFile =
            createTestDocx("simple_paragraph.docx") { document ->
                document.createParagraph().createRun().setText("Hello World")
            }

        val editor = WordDocumentEditorDocxImpl.createEditor(docFile)
        editor.replaceTextInDocument("World", "Universe")
        val outputFile = File(tempDir, "output_simple.docx")
        editor.saveToFile(outputFile)

        val resultDoc = XWPFDocument(outputFile.inputStream())
        val text = resultDoc.paragraphs.joinToString { it.text }
        assertTrue(text.contains("Universe"))
        assertFalse(text.contains("World"))
        resultDoc.close()
    }

    @Test
    fun `replaceTextInDocument replaces text in table cell`() {
        val docFile =
            createTestDocx("table_cell.docx") { document ->
                val table = document.createTable(1, 1)
                table.getRow(0).getCell(0).setText("Replace this text")
            }

        val editor = WordDocumentEditorDocxImpl.createEditor(docFile)
        editor.replaceTextInDocument("this", "that")
        val outputFile = File(tempDir, "output_table.docx")
        editor.saveToFile(outputFile)

        val resultDoc = XWPFDocument(outputFile.inputStream())
        val cellText =
            resultDoc.tables[0]
                .getRow(0)
                .getCell(0)
                .text
        assertTrue(cellText.contains("that"))
        assertFalse(cellText.contains("this"))
        resultDoc.close()
    }

    @Test
    fun `replaceTextInDocument replaces text in multiple table cells`() {
        val docFile =
            createTestDocx("multiple_cells.docx") { document ->
                val table = document.createTable(2, 2)
                table.getRow(0).getCell(0).setText("Name: {{NAME}}")
                table.getRow(0).getCell(1).setText("Age: {{AGE}}")
                table.getRow(1).getCell(0).setText("City: {{NAME}}")
                table.getRow(1).getCell(1).setText("Country: Unknown")
            }

        val editor = WordDocumentEditorDocxImpl.createEditor(docFile)
        editor.replaceTextInDocument("{{NAME}}", "John")
        editor.replaceTextInDocument("{{AGE}}", "25")
        val outputFile = File(tempDir, "output_multiple_cells.docx")
        editor.saveToFile(outputFile)

        val resultDoc = XWPFDocument(outputFile.inputStream())
        val table = resultDoc.tables[0]
        assertTrue(
            table
                .getRow(0)
                .getCell(0)
                .text
                .contains("John"),
        )
        assertTrue(
            table
                .getRow(0)
                .getCell(1)
                .text
                .contains("25"),
        )
        assertTrue(
            table
                .getRow(1)
                .getCell(0)
                .text
                .contains("John"),
        )
        assertFalse(table.text.contains("{{NAME}}"))
        assertFalse(table.text.contains("{{AGE}}"))
        resultDoc.close()
    }

    @Test
    fun `replaceTextInDocument does nothing when text not found`() {
        val docFile =
            createTestDocx("no_match.docx") { document ->
                document.createParagraph().createRun().setText("Hello World")
            }

        val editor = WordDocumentEditorDocxImpl.createEditor(docFile)
        editor.replaceTextInDocument("NotFound", "Replacement")
        val outputFile = File(tempDir, "output_no_match.docx")
        editor.saveToFile(outputFile)

        val resultDoc = XWPFDocument(outputFile.inputStream())
        val text = resultDoc.paragraphs.joinToString { it.text }
        assertEquals("Hello World", text)
        resultDoc.close()
    }

    @Test
    fun `replaceTextInDocument replaces all occurrences in paragraph`() {
        val docFile =
            createTestDocx("multiple_occurrences.docx") { document ->
                document.createParagraph().createRun().setText("cat and cat and cat")
            }

        val editor = WordDocumentEditorDocxImpl.createEditor(docFile)
        editor.replaceTextInDocument("cat", "dog")
        val outputFile = File(tempDir, "output_multiple_occurrences.docx")
        editor.saveToFile(outputFile)

        val resultDoc = XWPFDocument(outputFile.inputStream())
        val text = resultDoc.paragraphs.joinToString { it.text }
        assertEquals("dog and dog and dog", text)
        resultDoc.close()
    }

    @Test
    fun `replaceTextInDocument handles empty replacement string`() {
        val docFile =
            createTestDocx("empty_replacement.docx") { document ->
                document.createParagraph().createRun().setText("Hello World")
            }

        val editor = WordDocumentEditorDocxImpl.createEditor(docFile)
        editor.replaceTextInDocument("World", "")
        val outputFile = File(tempDir, "output_empty_replacement.docx")
        editor.saveToFile(outputFile)

        val resultDoc = XWPFDocument(outputFile.inputStream())
        val text = resultDoc.paragraphs.joinToString { it.text }
        assertEquals("Hello ", text)
        resultDoc.close()
    }

    @Test
    fun `replaceTextInDocument handles document with both paragraphs and tables`() {
        val docFile =
            createTestDocx("mixed_content.docx") { document ->
                document.createParagraph().createRun().setText("Header: {{TITLE}}")
                val table = document.createTable(1, 1)
                table.getRow(0).getCell(0).setText("Cell: {{TITLE}}")
                document.createParagraph().createRun().setText("Footer: {{TITLE}}")
            }

        val editor = WordDocumentEditorDocxImpl.createEditor(docFile)
        editor.replaceTextInDocument("{{TITLE}}", "Document Title")
        val outputFile = File(tempDir, "output_mixed.docx")
        editor.saveToFile(outputFile)

        val resultDoc = XWPFDocument(outputFile.inputStream())
        val paragraphTexts = resultDoc.paragraphs.map { it.text }
        val tableText =
            resultDoc.tables[0]
                .getRow(0)
                .getCell(0)
                .text

        assertTrue(paragraphTexts.any { it.contains("Header: Document Title") })
        assertTrue(paragraphTexts.any { it.contains("Footer: Document Title") })
        assertTrue(tableText.contains("Cell: Document Title"))
        assertFalse(resultDoc.paragraphs.any { it.text.contains("{{TITLE}}") })
        assertFalse(tableText.contains("{{TITLE}}"))
        resultDoc.close()
    }

    @Test
    fun `replaceTextInDocument handles nested table replacement`() {
        val docFile =
            createTestDocx("nested_table.docx") { document ->
                val outerTable = document.createTable(1, 1)
                val cell = outerTable.getRow(0).getCell(0)
                // Add a paragraph with text to the cell
                cell.paragraphs[0].createRun().setText("Outer: {{VALUE}}")
                // Create a nested table
                val innerTable = cell.insertNewTbl(cell.ctTc.addNewTbl().newCursor())
                val innerRow = innerTable.createRow()
                innerRow.addNewTableCell().setText("Inner: {{VALUE}}")
            }

        val editor = WordDocumentEditorDocxImpl.createEditor(docFile)
        editor.replaceTextInDocument("{{VALUE}}", "Test")
        val outputFile = File(tempDir, "output_nested.docx")
        editor.saveToFile(outputFile)

        val resultDoc = XWPFDocument(outputFile.inputStream())
        val outerCell = resultDoc.tables[0].getRow(0).getCell(0)

        // Check outer text and nested table text
        val allText = outerCell.text
        assertTrue(allText.contains("Test"))
        assertFalse(allText.contains("{{VALUE}}"))
        resultDoc.close()
    }

    @Test
    fun `replaceTextInDocument handles multiple rows in table`() {
        val docFile =
            createTestDocx("multiple_rows.docx") { document ->
                val table = document.createTable(3, 1)
                table.getRow(0).getCell(0).setText("Row 1: {{PLACEHOLDER}}")
                table.getRow(1).getCell(0).setText("Row 2: {{PLACEHOLDER}}")
                table.getRow(2).getCell(0).setText("Row 3: {{PLACEHOLDER}}")
            }

        val editor = WordDocumentEditorDocxImpl.createEditor(docFile)
        editor.replaceTextInDocument("{{PLACEHOLDER}}", "Data")
        val outputFile = File(tempDir, "output_rows.docx")
        editor.saveToFile(outputFile)

        val resultDoc = XWPFDocument(outputFile.inputStream())
        val table = resultDoc.tables[0]
        for (i in 0..2) {
            val cellText = table.getRow(i).getCell(0).text
            assertTrue(cellText.contains("Data"))
            assertFalse(cellText.contains("{{PLACEHOLDER}}"))
        }
        resultDoc.close()
    }

    @Test
    fun `replaceTextInDocument handles special characters in replacement`() {
        val docFile =
            createTestDocx("special_chars.docx") { document ->
                document.createParagraph().createRun().setText("Price: {{PRICE}}")
            }

        val editor = WordDocumentEditorDocxImpl.createEditor(docFile)
        editor.replaceTextInDocument("{{PRICE}}", "$100.00 (USD)")
        val outputFile = File(tempDir, "output_special.docx")
        editor.saveToFile(outputFile)

        val resultDoc = XWPFDocument(outputFile.inputStream())
        val text = resultDoc.paragraphs.joinToString { it.text }
        assertTrue(text.contains("$100.00 (USD)"))
        resultDoc.close()
    }

    private fun createTestDocx(
        fileName: String,
        configure: (XWPFDocument) -> Unit,
    ): File {
        val file = File(tempDir, fileName)
        XWPFDocument().use { document ->
            configure(document)
            file.outputStream().use { fos ->
                document.write(fos)
            }
        }
        return file
    }
}

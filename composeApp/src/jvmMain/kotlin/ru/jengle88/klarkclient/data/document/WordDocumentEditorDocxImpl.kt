package ru.jengle88.klarkclient.data.document

import org.apache.poi.xwpf.usermodel.PositionInParagraph
import org.apache.poi.xwpf.usermodel.TextSegment
import org.apache.poi.xwpf.usermodel.XWPFDocument
import org.apache.poi.xwpf.usermodel.XWPFParagraph
import org.apache.poi.xwpf.usermodel.XWPFTable
import org.apache.poi.xwpf.usermodel.XWPFTableCell
import org.apache.poi.xwpf.usermodel.XWPFTableRow
import ru.jengle88.klarkclient.domain.api.document.WordDocumentEditor
import java.io.File

class WordDocumentEditorDocxImpl private constructor(
    private val document: XWPFDocument,
) : WordDocumentEditor {
    override fun saveToFile(dstFile: File) {
        document.write(dstFile.outputStream())
    }

    override fun replaceTextInDocument(
        oldText: String,
        newText: String,
    ) {
        diveToTablesAndReplaceText(document.tables, oldText, newText)
        replaceTextInParagraphs(document.paragraphs, oldText, newText)
    }

    private fun diveToTablesAndReplaceText(
        tables: List<XWPFTable>,
        oldText: String,
        newText: String,
    ) {
        if (tables.isEmpty()) {
            return
        }
        for (table in tables) {
            diveToRowsAndReplace(table.rows, oldText, newText)
        }
    }

    private fun diveToRowsAndReplace(
        rows: List<XWPFTableRow>,
        oldText: String,
        newText: String,
    ) {
        if (rows.isEmpty()) {
            return
        }
        for (row in rows) {
            diveToCellsAndReplace(row.tableCells, oldText, newText)
        }
    }

    private fun diveToCellsAndReplace(
        cells: List<XWPFTableCell>,
        oldText: String,
        newText: String,
    ) {
        if (cells.isEmpty()) {
            return
        }
        for (cell in cells) {
            if (cell.tables.isNotEmpty()) {
                diveToTablesAndReplaceText(cell.tables, oldText, newText)
            }
            if (cell.paragraphs.isNotEmpty()) {
                replaceTextInParagraphs(cell.paragraphs, oldText, newText)
            }
        }
    }

    private fun replaceTextInParagraphs(
        paragraphs: List<XWPFParagraph>,
        oldText: String,
        newText: String,
    ) {
        for (paragraph in paragraphs) {
            var posInText: TextSegment? = paragraph.searchText(oldText, PositionInParagraph())
            while (posInText != null) {
                if (posInText.beginRun >= paragraph.runs.size || posInText.endRun >= paragraph.runs.size) {
                    // do nothing
                } else if (posInText.beginRun == posInText.endRun) {
                    val newText = paragraph.runs[posInText.beginRun].text().replace(oldText, newText)
                    paragraph.runs[posInText.beginRun].setText(newText, 0)
                } else {
                    val leftTextBeforeMask =
                        paragraph.runs[posInText.beginRun]
                            .text()
                            ?.dropLastWhile { it != oldText.first() }
                            ?.dropLast(1)
                    val rightTextAfterMask =
                        paragraph.runs[posInText.endRun]
                            .text()
                            ?.dropWhile { it != oldText.last() }
                            ?.drop(1)

                    for (i in posInText.endRun - 1 downTo posInText.beginRun + 1) {
                        paragraph.removeRun(i)
                    }
                    paragraph.runs[posInText.beginRun].setText(leftTextBeforeMask + newText, 0)
                    paragraph.runs[posInText.beginRun + 1].setText(rightTextAfterMask, 0)
                }
                posInText = paragraph.searchText(oldText, PositionInParagraph())
            }
        }
    }

    companion object Companion {
        fun createEditor(file: File): WordDocumentEditorDocxImpl {
            val document = XWPFDocument(file.inputStream())
            return WordDocumentEditorDocxImpl(document)
        }
    }
}

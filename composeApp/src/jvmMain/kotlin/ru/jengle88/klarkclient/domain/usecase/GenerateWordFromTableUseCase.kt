package ru.jengle88.klarkclient.domain.usecase

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import ru.jengle88.klarkclient.domain.api.document.WordDocumentEditorFactory
import java.io.File

class GenerateWordFromTableUseCase(
    private val wordDocumentEditorFactory: WordDocumentEditorFactory,
) {
    sealed interface WorkStatus {
        data object Start : WorkStatus

        data class Step(
            val message: String,
        ) : WorkStatus

        data class Finish(
            val message: String? = null,
            val cause: Throwable? = null,
        ) : WorkStatus
    }

    operator fun invoke(
        tableData: List<List<String>>,
        pathToTemplate: String,
        pathToDestination: String,
        ignoreLastNColumn: Int,
        unionLastNColumn: Int,
    ): Flow<WorkStatus> =
        flow {
            emit(WorkStatus.Start)
            val mapOfSuccessRowForTemplate = mutableMapOf<String, Int>()
            val generatedFolderName = "generated"
            try {
                for ((index, row) in tableData.withIndex()) {
                    val parseResult =
                        processRow(
                            row,
                            pathToTemplate,
                            fallbackFileName = "dstFile${index + 1}.docx",
                            pathToDestination,
                            generatedFolderName,
                        )

                    if (parseResult.isSuccess) {
                        val result = parseResult.getOrThrow()
                        emit(WorkStatus.Step("Готово: \"${result.destinationFileName}\" в папке \"${result.destinationFolderName}\""))
                        mapOfSuccessRowForTemplate[result.templateFolder] =
                            (mapOfSuccessRowForTemplate[result.templateFolder] ?: 0) + 1
                    } else {
                        val exception = parseResult.exceptionOrNull()?.message ?: "Ошибка"
                        emit(WorkStatus.Step(exception))
                    }
                }

                val amountOfGeneratedFilesInFolder = mutableMapOf<String, Int>()
                File(pathToDestination, generatedFolderName).listFiles()?.forEach { file ->
                    amountOfGeneratedFilesInFolder[file.name] =
                        (file.listFiles()?.filter { it.extension == "docx" }?.size ?: 0)
                }
                val finishResult =
                    buildString {
                        appendLine("Всего файлов в папке \"${generatedFolderName}\" = ${amountOfGeneratedFilesInFolder.values.sum()}")
                        amountOfGeneratedFilesInFolder.forEach { (folderName, amount) ->
                            appendLine(
                                "В папке \"$folderName\" сгенерировано файлов: $amount, успешных строк: ${mapOfSuccessRowForTemplate[folderName] ?: 0}",
                            )
                        }
                    }

                emit(WorkStatus.Finish(message = finishResult))
            } catch (e: Exception) {
                emit(WorkStatus.Finish(cause = e))
            }
        }

    private fun processRow(
        row: List<String>,
        pathToTemplate: String,
        fallbackFileName: String,
        pathToDestination: String,
        generatedFolderName: String,
    ): Result<ParseRowResult> {
        val templateFolder =
            getFolder(pathToTemplate, row)
                ?: return Result.failure(Exception("Ошибка: Папка \"${row.first()}\" не найдена"))

        val templateFile =
            getFile(templateFolder, "шаблон.docx")
                ?: return Result.failure(Exception("Ошибка: \"шаблон.docx\" не найден в папке \"${templateFolder}\" или недоступен!"))

        val masksFile =
            getFile(templateFolder, "маски.txt")
                ?: return Result.failure(Exception("Ошибка: \"маски.txt\" не найден в папке \"${templateFolder}\" или недоступен!"))

        val docxEditor =
            wordDocumentEditorFactory.create(templateFile)
                ?: return Result.failure(Exception("Ошибка: Не удалось создать редактор документа"))

        val masks = getMasks(masksFile)
        var filename = fallbackFileName
        val rowWithRemovedEmptyCell = row.filter { it.isNotEmpty() }.drop(1)
        masks.zip(rowWithRemovedEmptyCell).forEach { (mask, value) ->
            if (mask == "\$filename\$") {
                filename = parseFileName(value)
            } else if (mask != "") {
                docxEditor.replaceTextInDocument(mask, value)
            }
        }
        val destinationFolder = File(File(pathToDestination, generatedFolderName), templateFolder.name)
        if (!destinationFolder.exists()) {
            destinationFolder.mkdirs()
        }
        val destinationFile = File(destinationFolder, filename)
        docxEditor.saveToFile(destinationFile)
        return Result.success(ParseRowResult(destinationFile.name, destinationFolder.name, templateFolder.name))
    }

    private fun parseFileName(value: String): String {
        val fixedFilename =
            value
                .replace("\\", "_")
                .replace("/", "_")
        return "$fixedFilename.docx"
    }

    private fun getMasks(masksFile: File): List<String> =
        masksFile
            .readText()
            .split(";")
            .run {
                if (isNotEmpty() && last().isEmpty()) {
                    dropLast(1)
                } else {
                    this
                }
            }.map { mask -> mask.trim() }

    private fun getFolder(
        pathToTemplate: String,
        row: List<String>,
    ): File? {
        val templateFolderName = row.first()
        val folder = File(pathToTemplate, templateFolderName)
        if (templateFolderName.isEmpty() || !folder.exists()) {
            return null
        }
        return folder
    }

    private fun getFile(
        folder: File,
        fileName: String,
    ): File? {
        val file = File(folder, fileName)
        if (!file.exists() || !file.isFile || !file.canRead()) {
            return null
        }
        return file
    }

    private data class ParseRowResult(
        val destinationFileName: String,
        val destinationFolderName: String,
        val templateFolder: String,
    )
}

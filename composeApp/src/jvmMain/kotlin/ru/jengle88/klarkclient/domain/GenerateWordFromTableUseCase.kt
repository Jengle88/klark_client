package ru.jengle88.klarkclient.domain

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import ru.jengle88.klarkclient.data.WordDocumentEditor
import java.io.File

class GenerateWordFromTableUseCase {
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
                    val templateFolder = getFolder(pathToTemplate, row)
                    if (templateFolder == null) {
                        emit(WorkStatus.Step("Ошибка: Папка \"${row.first()}\" не найдена"))
                        continue
                    }

                    val templateFile = getFile(templateFolder, "шаблон.docx")
                    if (templateFile == null) {
                        emit(WorkStatus.Step("Ошибка: \"шаблон.docx\" не найден в папке \"${templateFolder}\" или недоступен!"))
                        continue
                    }

                    val masksFile = getFile(templateFolder, "маски.txt")
                    if (masksFile == null) {
                        emit(WorkStatus.Step("Ошибка: \"маски.txt\" не найден в папке \"${templateFolder}\" или недоступен!"))
                        continue
                    }

                    val docxEditor = WordDocumentEditor.createEditor(templateFile)

                    val masks = getMasks(masksFile)
                    var filename = "dstFile${index + 1}.docx"
                    masks.zip(row.filter { it.isNotEmpty() }.drop(1)).forEach { (mask, value) ->
                        if (mask == "\$filename\$") {
                            val fixedFilename =
                                value
                                    .replace("\\", "_")
                                    .replace("/", "_")
                            filename = "$fixedFilename.docx"
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
                    mapOfSuccessRowForTemplate[templateFolder.name] = (mapOfSuccessRowForTemplate[templateFolder.name] ?: 0) + 1
                    emit(WorkStatus.Step("Готово: \"${destinationFile.name}\" в папке \"${destinationFolder.name}\""))
                }

                val amountOfGeneratedFilesInFolder = mutableMapOf<String, Int>()
                File(pathToDestination, generatedFolderName).listFiles()?.forEach { file ->
                    amountOfGeneratedFilesInFolder[file.name] = (file.listFiles()?.filter { it.extension == "docx" }?.size ?: 0)
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
}

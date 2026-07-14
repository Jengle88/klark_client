package ru.jengle88.klarkclient.data.document

import java.io.File

data class TableConfiguration(
    val file: File,
    val ignoreLastNColumn: Int = 0,
    val unionLastNColumn: Int = 0,
    val xRange: IntRange? = null, // Опционально: граница по строкам (если используется чтение из диапазона)
    val yRange: IntRange? = null, // Опционально: граница по колонкам
)

data class TableContent(
    val rows: List<List<String>>,
)

data class TableData(
    val name: String, // Название таблицы/задачи (например, "Расчёт задолженности")
    val configuration: TableConfiguration,
    val content: TableContent? = null, // null, если таблица еще не была прочитана/вычислена
) {
    val fileName: String
        get() = configuration.file.name
}

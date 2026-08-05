package ru.jengle88.klarkclient.data.document

import java.io.File

data class TableConfiguration(
    val file: File,
    val ignoreLastNColumn: Int = 0,
    val unionLastNColumn: Int = 0,
    // Опционально: граница по строкам (если используется чтение из диапазона)
    val xRange: IntRange? = null,
    // Опционально: граница по колонкам
    val yRange: IntRange? = null,
)

data class TableContent(val rows: List<List<String>>)

package ru.jengle88.klarkclient.data.document

import java.io.File

/**
 * Настройки чтения табличных данных из файла.
 *
 * @property file файл с табличными данными
 * @property ignoreLastNColumn количество последних столбцов, которые нужно исключить
 * @property unionLastNColumn количество последних столбцов, которые нужно объединить в один
 */
data class TableConfiguration(
    val file: File,
    val ignoreLastNColumn: Int = 0,
    val unionLastNColumn: Int = 0,
)

/**
 * Содержимое таблицы, представленное списком строк.
 *
 * @property rows строки таблицы, каждая из которых содержит значения ячеек
 */
data class TableContent(val rows: List<List<String>>)

/**
 * Представляет группу строк таблицы, объединённых по значению первой ячейки.
 *
 * @property key ключ группировки
 * @property content содержимое таблицы для этой группы
 */
data class TableGroup(val key: String, val content: TableContent)

package ru.jengle88.klarkclient.data.document

import kotlinx.collections.immutable.ImmutableList

/**
 * Представляет группу строк таблицы, объединённых по значению первой ячейки.
 *
 * @property key ключ группировки
 * @property rows строки группы, включая заголовок таблицы в начале
 */
data class TableGroup(val key: String, val rows: ImmutableList<ImmutableList<String>>)

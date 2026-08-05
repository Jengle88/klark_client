package ru.jengle88.klarkclient.ui.datamodels

import kotlinx.collections.immutable.ImmutableList

/**
 * Содержимое таблицы для отображения в UI.
 *
 * @property rows неизменяемый список строк таблицы
 */
data class TableContentState(val rows: ImmutableList<ImmutableList<String>>)

/**
 * Группа строк таблицы для отображения в UI.
 *
 * @property key ключ группировки
 * @property content содержимое таблицы для этой группы
 */
data class TableGroupState(val key: String, val content: TableContentState)

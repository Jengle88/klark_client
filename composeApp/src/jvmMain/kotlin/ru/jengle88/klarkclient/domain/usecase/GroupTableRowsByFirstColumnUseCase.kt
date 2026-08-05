package ru.jengle88.klarkclient.domain.usecase

import ru.jengle88.klarkclient.data.document.TableContent
import ru.jengle88.klarkclient.data.document.TableGroup

/**
 * Группирует строки таблицы по значению первой ячейки.
 *
 * Первая строка считается заголовком и не участвует в группировке.
 * Строки с пустым значением в первой ячейке пропускаются.
 */
class GroupTableRowsByFirstColumnUseCase {
    operator fun invoke(rows: List<List<String>>): List<TableGroup> {
        if (rows.size < 2) return emptyList()

        return rows
            .asSequence()
            .drop(1)
            .filter { it.firstOrNull()?.isNotEmpty() == true }
            .groupBy { it.first() }
            .map { (key, groupRows) ->
                TableGroup(
                    key = key,
                    content = TableContent(groupRows),
                )
            }
    }
}

package ru.jengle88.klarkclient.domain.usecase

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue
import kotlinx.collections.immutable.persistentListOf

class GroupTableRowsByFirstColumnUseCaseTest {
    private val useCase = GroupTableRowsByFirstColumnUseCase()

    @Test
    fun `invoke groups rows by first column without duplicating header`() {
        val rows =
            listOf(
                listOf("Группа", "Имя", "Сумма"),
                listOf("А", "Иван", "100"),
                listOf("Б", "Пётр", "200"),
                listOf("А", "Мария", "300"),
            )

        val result = useCase(rows)

        assertEquals(2, result.size)
        assertEquals("А", result[0].key)
        assertEquals(
            persistentListOf(
                persistentListOf("А", "Иван", "100"),
                persistentListOf("А", "Мария", "300"),
            ),
            result[0].content.rows,
        )
        assertEquals("Б", result[1].key)
        assertEquals(
            persistentListOf(
                persistentListOf("Б", "Пётр", "200"),
            ),
            result[1].content.rows,
        )
    }

    @Test
    fun `invoke skips rows with empty first cell`() {
        val rows =
            listOf(
                listOf("Группа", "Имя"),
                listOf("А", "Иван"),
                listOf("", "Пётр"),
                listOf("А", "Мария"),
            )

        val result = useCase(rows)

        assertEquals(1, result.size)
        assertEquals("А", result[0].key)
        assertEquals(
            persistentListOf(
                persistentListOf("А", "Иван"),
                persistentListOf("А", "Мария"),
            ),
            result[0].content.rows,
        )
    }

    @Test
    fun `invoke preserves group order by first appearance`() {
        val rows =
            listOf(
                listOf("Группа", "Имя"),
                listOf("В", "Иван"),
                listOf("А", "Пётр"),
                listOf("В", "Мария"),
                listOf("А", "Ольга"),
            )

        val result = useCase(rows)

        assertEquals(listOf("В", "А"), result.map { it.key })
    }

    @Test
    fun `invoke returns empty list for single row table`() {
        val rows = listOf(listOf("Группа", "Имя"))

        val result = useCase(rows)

        assertTrue(result.isEmpty())
    }

    @Test
    fun `invoke returns empty list for empty table`() {
        val result = useCase(emptyList())

        assertTrue(result.isEmpty())
    }
}

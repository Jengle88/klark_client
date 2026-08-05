package ru.jengle88.klarkclient.domain.mapping

import kotlin.test.Test
import kotlin.test.assertEquals

class TemplateDataMappingTest {
    private val mapping = TemplateDataMapping()

    @Test
    fun `getRowValues removes empty cells from each row independently`() {
        val result = mapping.getRowValues(listOf("template", "first", "", "third"))

        assertEquals(listOf("first", "third"), result)
    }

    @Test
    fun `parseMasks preserves empty mask before trailing separator`() {
        val result = mapping.parseMasks($$$"$$first$$;;")

        assertEquals(listOf($$$"$$first$$", ""), result)
    }
}

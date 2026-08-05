package ru.jengle88.klarkclient.domain.usecase

import java.io.File
import kotlin.io.path.createTempDirectory
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue
import ru.jengle88.klarkclient.domain.mapping.TemplateDataMapping

class ReadTemplateMasksUseCaseTest {
    private val useCase = ReadTemplateMasksUseCase(TemplateDataMapping())

    @Test
    fun `invoke returns parsed masks from file`() {
        val tempDir = createTempDirectory("template").toFile()
        val templateDir = File(tempDir, "template1").apply { mkdirs() }
        File(templateDir, "маски.txt").writeText($$$"$$key1$$; $$key2$$ ;$$filename$$;")

        val result = useCase(tempDir.absolutePath, "template1")

        assertEquals(
            listOf(
                $$$"$$key1$$",
                $$$"$$key2$$",
                $$$"$$filename$$",
            ),
            result,
        )
        tempDir.deleteRecursively()
    }

    @Test
    fun `invoke returns empty list when masks file missing`() {
        val tempDir = createTempDirectory("template").toFile()

        val result = useCase(tempDir.absolutePath, "template1")

        assertTrue(result.isEmpty())
        tempDir.deleteRecursively()
    }

    @Test
    fun `invoke returns empty list when masks file is empty`() {
        val tempDir = createTempDirectory("template").toFile()
        val templateDir = File(tempDir, "template1").apply { mkdirs() }
        File(templateDir, "маски.txt").writeText("")

        val result = useCase(tempDir.absolutePath, "template1")

        assertTrue(result.isEmpty())
        tempDir.deleteRecursively()
    }

    @Test
    fun `invoke returns single mask without trailing separator`() {
        val tempDir = createTempDirectory("template").toFile()
        val templateDir = File(tempDir, "template1").apply { mkdirs() }
        File(templateDir, "маски.txt").writeText($$$"$$key1$$")

        val result = useCase(tempDir.absolutePath, "template1")

        assertEquals(listOf($$$"$$key1$$"), result)
        tempDir.deleteRecursively()
    }

    @Test
    fun `invoke preserves meaningful empty mask before trailing separator`() {
        val tempDir = createTempDirectory("template").toFile()
        val templateDir = File(tempDir, "template1").apply { mkdirs() }
        File(templateDir, "маски.txt").writeText($$$"$$key1$$;;")

        val result = useCase(tempDir.absolutePath, "template1")

        assertEquals(listOf($$$"$$key1$$", ""), result)
        tempDir.deleteRecursively()
    }
}

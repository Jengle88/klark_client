package ru.jengle88.klarkclient.domain.mapping

class TemplateDataMapping {
    fun parseMasks(content: String): List<String> = content
        .split(";")
        .let { masks ->
            if (masks.lastOrNull()?.isEmpty() == true) {
                masks.dropLast(1)
            } else {
                masks
            }
        }.map { mask -> mask.trim() }

    fun getRowValues(row: List<String>): List<String> =
        row.filter { cell -> cell.isNotEmpty() }.drop(1)
}

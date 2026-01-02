package ru.jengle88.klarkclient.data

import java.io.File

interface XlsxDataProvider {
    fun readData(
        table: File,
        ignoreLastNColumn: Int,
        unionLastNColumn: Int,
    ): List<List<String>>
}

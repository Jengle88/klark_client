package ru.jengle88.klarkclient.data

interface XlsxDataProvider {
    fun readData(
        path: String,
        ignoreLastNColumn: Int,
        unionLastNColumn: Int,
    ): List<List<String>>
}

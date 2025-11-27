package ru.jengle88.klerk_client.data

interface XlsxDataProvider {
    fun readData(path: String, ignoreLastNColumn: Int, unionLastNColumn: Int): List<List<String>>
}
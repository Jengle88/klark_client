package ru.jengle88.klerk_client.data

interface XlsxDataProvider {
    fun readData(path: String): List<List<String>>
}
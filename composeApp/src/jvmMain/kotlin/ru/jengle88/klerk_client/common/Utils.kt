package ru.jengle88.klerk_client.common

fun <T> List<T>.padLast(size: Int, value: T): List<T> {
    val prevList = this
    return buildList {
        addAll(prevList.take(size))
        if (size - prevList.size > 0) {
            addAll(List(size - prevList.size) { value })
        }
    }
}
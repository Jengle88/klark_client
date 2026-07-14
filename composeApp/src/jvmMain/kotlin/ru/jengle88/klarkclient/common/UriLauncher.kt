package ru.jengle88.klarkclient.common

interface UrlLauncher {
    @Throws(IllegalStateException::class)
    fun open(url: String)
}

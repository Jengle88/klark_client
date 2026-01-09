package ru.jengle88.klarkclient.data

import java.util.Properties

class AppConfig {
    private val properties = Properties()

    init {
        AppConfig::class.java.classLoader.getResourceAsStream("config.properties")?.use {
            properties.load(it)
        }
    }

    fun getProperty(key: String): String =
        properties.getProperty(key)?.takeIf { it.isNotEmpty() } ?: throw IllegalStateException("$key is not set")

    val clientId: String
        get() = getProperty("clientId")
    val clientSecret: String
        get() = getProperty("clientSecret")
}

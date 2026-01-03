package ru.jengle88.klarkclient.data

import java.util.Properties

class AppConfig {
    private val properties = Properties()

    init {
        val inputStream = object {}.javaClass.classLoader.getResourceAsStream("config.properties")
        if (inputStream != null) {
            properties.load(inputStream)
        }
    }

    fun getProperty(key: String): String? = properties.getProperty(key)

    val clientId: String get() = getProperty("clientId") ?: ""
    val clientSecret: String get() = getProperty("clientSecret") ?: ""
}

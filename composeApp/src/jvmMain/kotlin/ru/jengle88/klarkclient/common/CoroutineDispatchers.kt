package ru.jengle88.klarkclient.common

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.swing.Swing

class CoroutineDispatchers {

    val io
        get() = Dispatchers.IO

    val main
        get() = Dispatchers.Swing

    val default
        get() = Dispatchers.Default
}
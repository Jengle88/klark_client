package ru.jengle88.klerk_client.di

import org.koin.dsl.module
import ru.jengle88.klerk_client.ui.statemodels.MainTabScreenModel

val appModule = module {
    factory { MainTabScreenModel() }
}
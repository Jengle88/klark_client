package ru.jengle88.klarkclient.di

import org.koin.core.module.dsl.singleOf
import org.koin.dsl.module
import ru.jengle88.klarkclient.data.network.ai.AiHttpClient

val aiModule = module {
    includes(appModule)
    singleOf(::AiHttpClient)
}
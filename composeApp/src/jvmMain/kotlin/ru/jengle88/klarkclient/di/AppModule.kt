package ru.jengle88.klarkclient.di

import org.koin.core.module.dsl.factoryOf
import org.koin.dsl.module
import ru.jengle88.klarkclient.data.XlsxDataProvider
import ru.jengle88.klarkclient.data.XlsxDataProviderImpl
import ru.jengle88.klarkclient.domain.GenerateWordFromTableUseCase

val appModule =
    module {
        factory<XlsxDataProvider> { XlsxDataProviderImpl() }
        factoryOf(::GenerateWordFromTableUseCase)
    }

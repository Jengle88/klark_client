package ru.jengle88.klerk_client.di

import org.koin.core.module.dsl.factoryOf
import org.koin.dsl.module
import ru.jengle88.klerk_client.data.XlsxDataProvider
import ru.jengle88.klerk_client.data.XlsxDataProviderImpl
import ru.jengle88.klerk_client.domain.GenerateWordFromTableUseCase

val appModule = module {
    factory<XlsxDataProvider> { XlsxDataProviderImpl() }
    factoryOf(::GenerateWordFromTableUseCase)
}
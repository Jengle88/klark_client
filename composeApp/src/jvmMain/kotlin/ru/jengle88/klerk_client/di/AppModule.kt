package ru.jengle88.klerk_client.di

import org.koin.dsl.module
import ru.jengle88.klerk_client.data.XlsxDataProvider
import ru.jengle88.klerk_client.data.XlsxDataProviderImpl

val appModule = module {
    factory<XlsxDataProvider> { XlsxDataProviderImpl() }
}
package ru.jengle88.klarkclient.di

import org.koin.dsl.module
import ru.jengle88.klarkclient.common.CoroutineDispatchers
import ru.jengle88.klarkclient.common.DesktopUrlLauncherImpl
import ru.jengle88.klarkclient.common.UrlLauncher
import ru.jengle88.klarkclient.data.AppConfig
import ru.jengle88.klarkclient.domain.data.document.ExcelDocumentDataProvider
import ru.jengle88.klarkclient.data.document.ExcelDocumentDataProviderXlsxImpl
import ru.jengle88.klarkclient.data.document.WordDocumentEditorFactoryImpl
import ru.jengle88.klarkclient.data.network.auth.AuthCodeReceiver
import ru.jengle88.klarkclient.data.network.auth.AuthHttpClient
import ru.jengle88.klarkclient.data.network.auth.AuthManager
import ru.jengle88.klarkclient.data.network.auth.AuthProvider
import ru.jengle88.klarkclient.data.network.auth.AuthStore
import ru.jengle88.klarkclient.data.network.auth.AuthStoreImpl
import ru.jengle88.klarkclient.data.network.auth.KtorAuthCodeReceiver
import ru.jengle88.klarkclient.data.network.auth.YandexAuthProvider
import ru.jengle88.klarkclient.domain.data.document.WordDocumentEditorFactory

val appModule =
    module {
        single { CoroutineDispatchers() }
        single { AppConfig() }
        single { AuthHttpClient() }
        single<AuthProvider> {
            val appConfig: AppConfig = get()
            YandexAuthProvider(
                clientId = appConfig.clientId,
                clientSecret = appConfig.clientSecret
            )
        }
        single<AuthStore> { AuthStoreImpl() }
        single<UrlLauncher> { DesktopUrlLauncherImpl() }
        single<AuthCodeReceiver> { KtorAuthCodeReceiver() }
        single<AuthManager> {
            val appConfig: AppConfig = get()
            AuthManager(
                authHttpClient = get(),
                uriLauncher = get(),
                authCodeReceiver = get(),
                port = appConfig.authPort,
                ioDispatcher = get<CoroutineDispatchers>().io,
            )
        }
        factory<WordDocumentEditorFactory> { WordDocumentEditorFactoryImpl() }
        factory<ExcelDocumentDataProvider> { ExcelDocumentDataProviderXlsxImpl() }
    }

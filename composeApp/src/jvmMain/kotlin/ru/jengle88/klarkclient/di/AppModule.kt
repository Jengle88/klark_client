package ru.jengle88.klarkclient.di

import org.koin.core.module.dsl.factoryOf
import org.koin.core.module.dsl.singleOf
import org.koin.dsl.module
import ru.jengle88.klarkclient.common.CoroutineDispatchers
import ru.jengle88.klarkclient.common.DesktopUrlLauncherImpl
import ru.jengle88.klarkclient.common.UrlLauncher
import ru.jengle88.klarkclient.data.AppConfig
import ru.jengle88.klarkclient.data.document.ExcelDocumentDataProviderXlsxImpl
import ru.jengle88.klarkclient.data.document.WordDocumentEditorFactoryImpl
import ru.jengle88.klarkclient.data.network.auth.AuthHttpClient
import ru.jengle88.klarkclient.data.network.auth.AuthManagerImpl
import ru.jengle88.klarkclient.data.network.auth.AuthStoreImpl
import ru.jengle88.klarkclient.data.network.auth.KtorAuthCodeReceiver
import ru.jengle88.klarkclient.data.network.auth.YandexAuthProvider
import ru.jengle88.klarkclient.domain.api.auth.AuthCodeReceiver
import ru.jengle88.klarkclient.domain.api.auth.AuthManager
import ru.jengle88.klarkclient.domain.api.auth.AuthProvider
import ru.jengle88.klarkclient.domain.api.auth.AuthStore
import ru.jengle88.klarkclient.domain.api.document.ExcelDocumentDataProvider
import ru.jengle88.klarkclient.domain.api.document.WordDocumentEditorFactory

val appModule =
    module {
        single { CoroutineDispatchers() }
        single { AppConfig() }
        single { AuthHttpClient() }
        single<AuthProvider> {
            val appConfig: AppConfig = get()
            YandexAuthProvider(
                clientId = appConfig.clientId,
                clientSecret = appConfig.clientSecret,
            )
        }
        singleOf<AuthStore>(::AuthStoreImpl)
        factoryOf<UrlLauncher>(::DesktopUrlLauncherImpl)
        singleOf<AuthCodeReceiver>(::KtorAuthCodeReceiver)
        single<AuthManager> {
            val appConfig: AppConfig = get()
            AuthManagerImpl(
                authHttpClient = get(),
                authProvider = get(),
                uriLauncher = get(),
                authCodeReceiver = get(),
                port = appConfig.authPort,
                ioDispatcher = get<CoroutineDispatchers>().io,
            )
        }
        singleOf<WordDocumentEditorFactory>(::WordDocumentEditorFactoryImpl)
        factoryOf<ExcelDocumentDataProvider>(::ExcelDocumentDataProviderXlsxImpl)
    }

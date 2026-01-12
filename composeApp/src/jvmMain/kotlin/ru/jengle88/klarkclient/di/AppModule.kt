package ru.jengle88.klarkclient.di

import org.koin.core.module.dsl.factoryOf
import org.koin.dsl.module
import ru.jengle88.klarkclient.common.CoroutineDispatchers
import ru.jengle88.klarkclient.common.DesktopUrlLauncherImpl
import ru.jengle88.klarkclient.common.UrlLauncher
import ru.jengle88.klarkclient.data.AppConfig
import ru.jengle88.klarkclient.data.XlsxDataProvider
import ru.jengle88.klarkclient.data.XlsxDataProviderImpl
import ru.jengle88.klarkclient.data.network.auth.AuthCodeReceiver
import ru.jengle88.klarkclient.data.network.auth.AuthHttpClient
import ru.jengle88.klarkclient.data.network.auth.AuthManager
import ru.jengle88.klarkclient.data.network.auth.AuthProvider
import ru.jengle88.klarkclient.data.network.auth.AuthStore
import ru.jengle88.klarkclient.data.network.auth.AuthStoreImpl
import ru.jengle88.klarkclient.data.network.auth.KtorAuthCodeReceiver
import ru.jengle88.klarkclient.data.network.auth.YandexAuthProvider
import ru.jengle88.klarkclient.domain.GenerateWordFromTableUseCase

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
        single<AuthStore> { AuthStoreImpl() }
        single<UrlLauncher> { DesktopUrlLauncherImpl() }
        single<AuthCodeReceiver> { KtorAuthCodeReceiver() }
        single {
            AuthManager(
                authHttpClient = get(),
                uriLauncher = get(),
                authCodeReceiver = get(),
                ioDispatcher = get<CoroutineDispatchers>().io,
            )
        }
        factory<XlsxDataProvider> { XlsxDataProviderImpl() }
        factoryOf(::GenerateWordFromTableUseCase)
    }

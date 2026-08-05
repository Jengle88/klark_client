package ru.jengle88.klarkclient.di

import org.koin.core.module.dsl.factoryOf
import org.koin.core.module.dsl.singleOf
import org.koin.dsl.module
import ru.jengle88.klarkclient.common.CoroutineDispatchers
import ru.jengle88.klarkclient.common.DesktopUrlLauncherImpl
import ru.jengle88.klarkclient.common.UrlLauncher
import ru.jengle88.klarkclient.data.document.ExcelDocumentDataProviderXlsxImpl
import ru.jengle88.klarkclient.data.document.WordDocumentEditorFactoryImpl
import ru.jengle88.klarkclient.data.network.HttpClientFactory
import ru.jengle88.klarkclient.domain.api.document.ExcelDocumentDataProvider
import ru.jengle88.klarkclient.domain.api.document.WordDocumentEditorFactory
import ru.jengle88.klarkclient.domain.api.update.UpdateChecker
import ru.jengle88.klarkclient.domain.mapping.TemplateDataMapping

val appModule =
    module {
        single { CoroutineDispatchers() }
        single { HttpClientFactory.create() }
        factoryOf<UrlLauncher>(::DesktopUrlLauncherImpl)
        singleOf<WordDocumentEditorFactory>(::WordDocumentEditorFactoryImpl)
        factoryOf<ExcelDocumentDataProvider>(::ExcelDocumentDataProviderXlsxImpl)
        factoryOf(::TemplateDataMapping)

        single {
            UpdateChecker(
                client = get(),
                owner = "Jengle88",
                repo = "klark_client",
            )
        }
    }

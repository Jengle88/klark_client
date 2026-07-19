package ru.jengle88.klarkclient.di

import org.koin.core.module.dsl.factoryOf
import org.koin.core.module.dsl.singleOf
import org.koin.dsl.module
import ru.jengle88.klarkclient.ui.generatedocs.GenerateDocsStateModel
import ru.jengle88.klarkclient.ui.generatedocs.generationdialog.GenerateDocsGenerationDialogStateModel
import ru.jengle88.klarkclient.ui.maintab.MainTabScreenModel

val screenModelModule =
    module {
        includes(useCaseModule)
        // Auth feature is disabled in UI but kept in code.
        // singleOf(::AuthViewScreenModel)
        singleOf(::MainTabScreenModel)
        factoryOf(::GenerateDocsStateModel)
        factoryOf(::GenerateDocsGenerationDialogStateModel)
    }

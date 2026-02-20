package ru.jengle88.klarkclient.di

import org.koin.core.module.dsl.factoryOf
import org.koin.core.module.dsl.singleOf
import org.koin.dsl.module
import ru.jengle88.klarkclient.ui.auth.AuthViewScreenModel
import ru.jengle88.klarkclient.ui.generatedocs.GenerateDocsStateModel
import ru.jengle88.klarkclient.ui.generatedocs.generationdialog.GenerateDocsGenerationDialogStateModel
import ru.jengle88.klarkclient.ui.maintab.MainTabScreenModel

val screenModelModule =
    module {
        includes(useCaseModule)
        singleOf(::AuthViewScreenModel)
        singleOf(::MainTabScreenModel)
        factoryOf(::GenerateDocsStateModel)
        factoryOf(::GenerateDocsGenerationDialogStateModel)
    }

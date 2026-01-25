package ru.jengle88.klarkclient.di

import org.koin.core.module.dsl.factoryOf
import org.koin.core.module.dsl.singleOf
import org.koin.dsl.module
import ru.jengle88.klarkclient.ui.auth.AuthScreenModel
import ru.jengle88.klarkclient.ui.generatedocs.GenerateDocsStateModel
import ru.jengle88.klarkclient.ui.generatedocs.generationdialog.GenerateDocsGenerationDialogStateModel
import ru.jengle88.klarkclient.ui.maintab.MainTabScreenModel

val screenModelModule =
    module {
        singleOf(::AuthScreenModel)
        factoryOf(::MainTabScreenModel)
        factory<GenerateDocsStateModel> {
            GenerateDocsStateModel(
                excelDocumentDataProvider = get(),
            )
        }
        factory<GenerateDocsGenerationDialogStateModel> {
            GenerateDocsGenerationDialogStateModel(generateWordFromTableUseCase = get())
        }
    }

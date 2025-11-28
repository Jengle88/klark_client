package ru.jengle88.klerk_client.di

import org.koin.core.module.dsl.factoryOf
import org.koin.dsl.module
import ru.jengle88.klerk_client.ui.generatedocs.GenerateDocsStateModel
import ru.jengle88.klerk_client.ui.generatedocs.generationdialog.GenerateDocsGenerationDialogStateModel
import ru.jengle88.klerk_client.ui.maintab.MainTabScreenModel

val screenModelModule = module {
    factoryOf(::MainTabScreenModel)
    factory<GenerateDocsStateModel> {
        GenerateDocsStateModel(
            xlsxDataProvider = get(),
        )
    }
    factory<GenerateDocsGenerationDialogStateModel> {
        GenerateDocsGenerationDialogStateModel(generateWordFromTableUseCase = get())
    }
}
package ru.jengle88.klarkclient.di

import org.koin.core.module.dsl.factoryOf
import org.koin.dsl.module
import ru.jengle88.klarkclient.domain.usecase.GenerateWordFromTableUseCase
import ru.jengle88.klarkclient.domain.usecase.ReadTableDataUseCase

val useCaseModule = module {
    includes(appModule)
    factoryOf(::GenerateWordFromTableUseCase)
    factoryOf(::ReadTableDataUseCase)

}
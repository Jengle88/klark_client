package ru.jengle88.klarkclient.di

import org.koin.core.module.dsl.factoryOf
import org.koin.dsl.module
import ru.jengle88.klarkclient.domain.usecase.GenerateWordFromTableUseCase
import ru.jengle88.klarkclient.domain.usecase.GroupTableRowsByFirstColumnUseCase
import ru.jengle88.klarkclient.domain.usecase.ReadTableDataUseCase
import ru.jengle88.klarkclient.domain.usecase.ReadTemplateMasksUseCase

val useCaseModule =
    module {
        includes(appModule)

        // tables
        factoryOf(::GenerateWordFromTableUseCase)
        factoryOf(::GroupTableRowsByFirstColumnUseCase)
        factoryOf(::ReadTableDataUseCase)
        factoryOf(::ReadTemplateMasksUseCase)
    }

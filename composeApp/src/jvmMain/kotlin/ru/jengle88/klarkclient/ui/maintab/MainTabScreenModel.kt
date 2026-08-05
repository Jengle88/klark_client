package ru.jengle88.klarkclient.ui.maintab

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.GeneratingTokens
import cafe.adriel.voyager.core.model.ScreenModel
import cafe.adriel.voyager.core.model.screenModelScope
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.persistentListOf
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import ru.jengle88.klarkclient.BuildInfo
import ru.jengle88.klarkclient.common.CoroutineDispatchers
import ru.jengle88.klarkclient.common.UrlLauncher
import ru.jengle88.klarkclient.domain.api.update.UpdateChecker
import ru.jengle88.klarkclient.domain.api.update.UpdateInfo
import ru.jengle88.klarkclient.ui.datamodels.AppFeatureVO
import ru.jengle88.klarkclient.ui.datamodels.AppScreenDestination

class MainTabScreenModel(
    private val updateChecker: UpdateChecker,
    private val coroutineDispatchers: CoroutineDispatchers,
    private val urlLauncher: UrlLauncher,
) : ScreenModel {
    private val _state = MutableStateFlow<ImmutableList<AppFeatureVO>>(persistentListOf())
    val state: StateFlow<ImmutableList<AppFeatureVO>> = _state.asStateFlow()

    private val _updateInfo = MutableStateFlow<UpdateInfo?>(null)
    val updateInfo: StateFlow<UpdateInfo?> = _updateInfo.asStateFlow()

    init {
        _state.update {
            persistentListOf(
                AppFeatureVO(
                    title = "Генерация документов",
                    description = "Генерация Word-документов по Excel-таблице",
                    icon = Icons.Default.GeneratingTokens,
                    route = AppScreenDestination.GENERATION,
                ),
                /*AppFeatureVO(
                    title = "Калькулятор периодов",
                    description = "Расчет задолженности по ЛС с группировкой дат",
                    icon = Icons.Default.DateRange,
                    route = AppScreenDestination.DEBT_CALCULATOR,
                ),
                AppFeatureVO(
                    title = "База знаний",
                    description = "Готовые формулировки и шаблоны документов",
                    icon = Icons.Default.Book,
                    route = AppScreenDestination.TEMPLATES,
                ),
                AppFeatureVO(
                    title = "Лингвистика",
                    description = "Склонение ФИО, определение пола, сумма прописью",
                    icon = Icons.Default.Translate, // Или Edit
                    route = AppScreenDestination.TEXT_UTILS,
                ),
                AppFeatureVO(
                    title = "Подсудность",
                    description = "Поиск судебного участка по адресу должника",
                    icon = Icons.Default.LocationOn,
                    route = AppScreenDestination.JURISDICTION,
                ),
                 */
            )
        }

        checkForUpdates()
    }

    private fun checkForUpdates() {
        screenModelScope.launch(coroutineDispatchers.io) {
            val updateInfo = updateChecker.checkForUpdate(BuildInfo.VERSION)
            if (updateInfo != null) {
                _updateInfo.update { updateInfo }
            }
        }
    }

    fun onUpdateClick() {
        val url = _updateInfo.value?.downloadUrl ?: return
        try {
            urlLauncher.open(url)
        } catch (_: Exception) {
            // Desktop/Browser не поддерживается — ничего не делаем
        }
    }
}

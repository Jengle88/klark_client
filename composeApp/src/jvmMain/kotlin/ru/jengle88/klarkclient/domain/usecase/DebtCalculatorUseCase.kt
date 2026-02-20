package ru.jengle88.klarkclient.domain.usecase

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import ru.jengle88.klarkclient.domain.data.document.ExcelDocumentDataProvider
import java.io.File

class DebtCalculatorUseCase(
    private val excelDocumentDataProvider: ExcelDocumentDataProvider,

    ) {

    sealed interface WorkStatus {
        data object Start : WorkStatus

        data class TableDataReceived(
            val amountOfAllFiles: Int,
        ): WorkStatus

        data class ProcessingStep(
            val amountOfProcessedFiles: Int,
            val amountOfFailedFiles: Int,
            val amountOfAllFiles: Int,
        ) : WorkStatus

        data class Finish(
            val message: String? = null,
            val cause: Throwable? = null,
        ) : WorkStatus
    }

    operator fun invoke(
        pathToTables: File,
        pathToDestination: String,
    ): Flow<WorkStatus> =
        flow {
            emit(WorkStatus.Start)
            if (!pathToTables.exists() || !pathToTables.isDirectory) {
                emit(WorkStatus.Finish(cause = Exception("Выбранной папки с данными не существует")))
                return@flow
            }
            val tablesPath = pathToTables.listFiles { file -> file.extension == "xlsx" }
            if (tablesPath.isNullOrEmpty()) {
                emit(WorkStatus.Finish(cause = Exception("В выбранной папке не найдены Excel-таблицы (формат xlsx)")))
                return@flow
            }
            emit(WorkStatus.TableDataReceived(tablesPath.size))
            for (tablePath in tablesPath) {
                val table = excelDocumentDataProvider.readData(
                    tablePath,
                    0,
                    0,
                )

            }
        }

    private companion object {
        const val PROMPT = """
            ### РОЛЬ
            Ты — эксперт по анализу финансовых документов и выписок ЖКХ.
            
            ### ЗАДАЧА
            На основе предоставленного текста «Расчета задолженности» необходимо сформировать список временных интервалов, в которых у пользователя фактически сохранялся долг.
            
            ### АЛГОРИТМ ДЕЙСТВИЙ
            1. Просканируй колонку «Остаток долга». Игнорируй строки, где остаток равен 0,00 или пуст.
            2. Для строк, где остаток > 0, определи соответствующий месяц и год начисления.
            3. Сгруппируй идущие подряд месяцы в единые периоды. 
               - Начало периода: 1-е число первого месяца задолженности.
               - Конец периода: последняя календарная дата последнего месяца задолженности.
            4. При определении последней даты февраля обязательно учитывай, является ли год високосным (29 февраля) или обычным (28 февраля).
            5. Если между месяцами с долгом встречается месяц с нулевым остатком, зафиксируй разрыв и начни новый период.
            6. Если период - один месяц, то не нужно писать день - просто укажи месяц и год
            
            ### ПРИМЕР ВХОДНЫХ ДАННЫХ
            "
            Дата; Значение 1; Значение 2; Остаток долга 
            Январь 2024; 1000,00; 0,00; ; 1000,00
            Февраль 2024; 1000,00; 0,00; ; 1000,00
            Март 2024; 1000,00; 0,00; 0,00;
            Апрель 2024; 1000,00; 0,00; 1000,00;"
            
            ### ПРИМЕР ВЫХОДНЫХ ДАННЫХ (ФОРМАТ)
            1 января 2024 года — 29 февраля 2024 года, апрель 2024 года
            
            ### ОГРАНИЧЕНИЯ
            - Выводи только даты периодов. 
            - Запрещено добавлять вводные фразы, пояснения, промежуточные расчеты или комментарии.
        """
    }
}
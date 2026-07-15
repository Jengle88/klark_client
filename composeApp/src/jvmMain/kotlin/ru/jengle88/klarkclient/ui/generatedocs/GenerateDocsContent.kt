package ru.jengle88.klarkclient.ui.generatedocs

import androidx.compose.desktop.ui.tooling.preview.Preview
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import kotlinx.collections.immutable.persistentListOf
import ru.jengle88.klarkclient.ui.components.NumberInputField
import ru.jengle88.klarkclient.ui.components.PathInputField
import ru.jengle88.klarkclient.ui.components.table.TableStyle
import ru.jengle88.klarkclient.ui.components.table.TableView

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GenerateDocsContent(
    state: GenerateDocsParamsState,
    onIntent: (GenerateDocsIntent) -> Unit,
    onEffect: (GenerateDocsEffect) -> Unit,
    onBack: () -> Unit,
) {
    Scaffold(
        topBar = {
            TopAppBar(
                colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.secondaryContainer),
                title = {
                    if (state.isGenerating || state.isTableLoading) {
                        LinearProgressIndicator(modifier = Modifier.fillMaxWidth())
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Назад")
                    }
                },
            )
        },
    ) { paddingValues ->
        Row(modifier = Modifier.padding(paddingValues)) {
            Column(
                modifier =
                    Modifier
                        .fillMaxHeight()
                        .fillMaxWidth(0.3f)
                        .padding(16.dp)
                        .verticalScroll(rememberScrollState()),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(16.dp),
            ) {
                Text(
                    "Генерация документов",
                    style = MaterialTheme.typography.headlineMedium,
                )

                PathInputField(
                    label = "Путь к таблице с данными",
                    path = state.pathToTable,
                    onPathChange = { onIntent(GenerateDocsIntent.UpdatePathToTable(it)) },
                    onBrowseClick = { onEffect(GenerateDocsEffect.ShowTablePicker) },
                )

                PathInputField(
                    label = "Путь к папке с шаблоном",
                    path = state.pathToTemplate,
                    onPathChange = { onIntent(GenerateDocsIntent.UpdatePathToTemplate(it)) },
                    onBrowseClick = { onEffect(GenerateDocsEffect.ShowTemplatePicker) },
                )

                PathInputField(
                    label = "Путь для сохранения документов",
                    path = state.pathToDestination,
                    onPathChange = { onIntent(GenerateDocsIntent.UpdatePathToDestination(it)) },
                    onBrowseClick = { onEffect(GenerateDocsEffect.ShowDestinationPicker) },
                )

                NumberInputField(
                    label = "Исключить последние N колонок",
                    value = state.ignoreLastNColumn,
                    onValueChange = { onIntent(GenerateDocsIntent.UpdateIgnoreLastNColumn(it)) },
                )

                NumberInputField(
                    label = "Объединить последние N колонок",
                    value = state.unionLastNColumn,
                    onValueChange = { onIntent(GenerateDocsIntent.UpdateUnionLastNColumn(it)) },
                )

                Spacer(modifier = Modifier.height(16.dp))

                Button(
                    onClick = { onIntent(GenerateDocsIntent.StartGenerating) },
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    Text("Сгенерировать")
                }
            }
            VerticalDivider()
            Column(
                verticalArrangement = Arrangement.spacedBy(16.dp),
            ) {
                Box(
                    modifier = Modifier.fillMaxWidth().padding(vertical = 16.dp),
                    contentAlignment = Alignment.Center,
                ) {
                    Text(
                        "Просмотр данных",
                        style = MaterialTheme.typography.headlineMedium,
                    )
                }
                TableView(
                    data = state.tableData,
                    style = TableStyle(isAlternatingRowColorsEnabled = true),
                )
            }
        }
    }
}

@Preview
@Composable
fun PreviewGenerateDocsScreen() {
    GenerateDocsContent(
        state =
            GenerateDocsParamsState(
                pathToTable = "C:/Users/user/Documents/data.xlsx",
                pathToTemplate = "C:/Users/user/Documents/template.docx",
                pathToDestination = "C:/Users/user/Documents/output",
                ignoreLastNColumn = 1,
                unionLastNColumn = 2,
                isGenerating = false,
                isTableLoading = false,
                tableData =
                    persistentListOf(
                        persistentListOf("Заголовок 1", "Заголовок 2", "Заголовок 3", "Заголовок 4"),
                        persistentListOf("Данные 1.1", "Данные 1.2", "Данные 1.3", "Данные 1.4"),
                        persistentListOf("Данные 2.1", "Данные 2.2", "Данные 2.3", "Данные 2.4"),
                        persistentListOf("Данные 3.1", "Данные 3.2", "Данные 3.3", "Данные 3.4"),
                        persistentListOf(
                            "Данные 4.1",
                            "Данные 4.2",
                            "Данные 4.3",
                            "Данные 4.4",
                        ),
                    ),
            ),
        onIntent = {},
        onEffect = {},
        onBack = {},
    )
}

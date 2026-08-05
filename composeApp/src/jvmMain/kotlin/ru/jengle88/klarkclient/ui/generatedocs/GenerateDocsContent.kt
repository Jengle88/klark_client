package ru.jengle88.klarkclient.ui.generatedocs

import androidx.compose.desktop.ui.tooling.preview.Preview
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.selection.toggleable
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.Checkbox
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.VerticalDivider
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.unit.dp
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.persistentListOf
import kotlinx.collections.immutable.persistentMapOf
import ru.jengle88.klarkclient.ui.components.NumberInputField
import ru.jengle88.klarkclient.ui.components.PathInputField
import ru.jengle88.klarkclient.ui.components.table.TableStyle
import ru.jengle88.klarkclient.ui.components.table.TableView
import ru.jengle88.klarkclient.ui.datamodels.TableContentState
import ru.jengle88.klarkclient.ui.datamodels.TableGroupState

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
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.secondaryContainer,
                ),
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
                actions = {
                    TextButton(
                        onClick = { onIntent(GenerateDocsIntent.ShowInfo) },
                    ) {
                        Text("Информация о режиме")
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

                val isTableControlsEnabled = !state.isTableLoading

                Row(
                    modifier =
                    Modifier
                        .fillMaxWidth()
                        .toggleable(
                            value = state.isTableGrouped,
                            onValueChange = {
                                onIntent(GenerateDocsIntent.UpdateIsTableGrouped(it))
                            },
                            role = Role.Checkbox,
                            enabled = isTableControlsEnabled,
                        )
                        .padding(vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Checkbox(
                        checked = state.isTableGrouped,
                        onCheckedChange = null,
                        enabled = isTableControlsEnabled,
                    )
                    Text(
                        text = "Группировать по шаблону",
                        style = MaterialTheme.typography.bodyLarge,
                        modifier = Modifier.padding(start = 12.dp),
                        color = if (isTableControlsEnabled) {
                            MaterialTheme.colorScheme.onSurface
                        } else {
                            MaterialTheme.colorScheme.onSurface.copy(alpha = 0.38f)
                        },
                    )
                }

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
                modifier =
                Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp),
            ) {
                Box(
                    modifier = Modifier.fillMaxWidth(),
                    contentAlignment = Alignment.Center,
                ) {
                    Text(
                        "Просмотр данных",
                        style = MaterialTheme.typography.headlineMedium,
                    )
                }
                if (state.isTableGrouped && state.tableGroups.isNotEmpty()) {
                    state.tableGroups.forEach { group ->
                        GroupedTableView(
                            group = group,
                            customHeaders = state.masksByGroupKey[group.key] ?: persistentListOf(),
                        )
                    }
                } else {
                    TableView(
                        data = state.tableData.rows,
                        style = TableStyle(
                            isAlternatingRowColorsEnabled = true,
                            isVerticalScrollable = false,
                        ),
                    )
                }
            }
        }
    }
}

@Composable
private fun GroupedTableView(group: TableGroupState, customHeaders: ImmutableList<String>) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text(
            text = "Группа: ${group.key}",
            style = MaterialTheme.typography.titleMedium,
        )
        TableView(
            data = group.content.rows,
            style = TableStyle(
                isAlternatingRowColorsEnabled = true,
                isVerticalScrollable = false,
                isFirstColumnVisible = false,
            ),
            customHeaders = customHeaders,
        )
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
            isTableGrouped = true,
            isGenerating = false,
            isTableLoading = false,
            tableData =
            TableContentState(
                persistentListOf(
                    persistentListOf("Группа", "Заголовок 2", "Заголовок 3", "Заголовок 4"),
                    persistentListOf("А", "Данные А.1", "Данные А.2", "Данные А.3"),
                    persistentListOf("А", "Данные А.3", "Данные А.4", "Данные А.5"),
                    persistentListOf("Б", "Данные Б.1", "Данные Б.2", "Данные Б.3"),
                    persistentListOf(
                        "Б",
                        "Данные Б.4",
                        "Данные Б.5",
                        "Данные Б.6",
                    ),
                ),
            ),
            tableGroups =
            persistentListOf(
                TableGroupState(
                    key = "А",
                    content =
                    TableContentState(
                        persistentListOf(
                            persistentListOf("А", "Данные А.1", "Данные А.2", "Данные А.3"),
                            persistentListOf("А", "Данные А.3", "Данные А.4", "Данные А.5"),
                        ),
                    ),
                ),
                TableGroupState(
                    key = "Б",
                    content =
                    TableContentState(
                        persistentListOf(
                            persistentListOf("Б", "Данные Б.1", "Данные Б.2", "Данные Б.3"),
                            persistentListOf("Б", "Данные Б.4", "Данные Б.5", "Данные Б.6"),
                        ),
                    ),
                ),
            ),
            masksByGroupKey =
            persistentMapOf(
                "А" to persistentListOf($$$"$$key1$$", $$$"$$key2$$", $$$"$$key3$$"),
                "Б" to persistentListOf($$$"$$key1$$", $$$"$$key2$$", $$$"$$filename$$"),
            ),
        ),
        onIntent = {},
        onEffect = {},
        onBack = {},
    )
}

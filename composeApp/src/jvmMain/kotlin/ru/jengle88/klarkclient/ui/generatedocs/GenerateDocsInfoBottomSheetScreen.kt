package ru.jengle88.klarkclient.ui.generatedocs

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import cafe.adriel.voyager.core.screen.Screen
import cafe.adriel.voyager.navigator.bottomSheet.LocalBottomSheetNavigator
import com.mikepenz.markdown.compose.components.markdownComponents
import com.mikepenz.markdown.compose.elements.MarkdownTable
import com.mikepenz.markdown.m3.Markdown
import com.mikepenz.markdown.m3.markdownTypography

class GenerateDocsInfoBottomSheetScreen : Screen {
    @Composable
    override fun Content() {
        val bottomSheetNavigator = LocalBottomSheetNavigator.current

        InfoBottomSheetContent(
            title = INFO_TITLE,
            description = INFO_DESCRIPTION,
            onDismiss = bottomSheetNavigator::hide,
        )
    }

    @Composable
    private fun WrappedMarkdownTableRow(
        content: String,
        row: org.intellij.markdown.ast.ASTNode,
        style: TextStyle,
        isHeader: Boolean,
    ) {
        val cellType = org.intellij.markdown.flavours.gfm.GFMTokenTypes.CELL
        val cells = row.children.filter { it.type == cellType }
        val backgroundColor = if (isHeader) {
            MaterialTheme.colorScheme.surfaceContainerHighest
        } else {
            MaterialTheme.colorScheme.surfaceContainerLowest
        }

        Row(
            modifier = Modifier.fillMaxWidth().background(backgroundColor),
        ) {
            cells.forEach { cell ->
                Column(
                    modifier =
                        Modifier
                            .weight(1f)
                            .padding(8.dp),
                ) {
                    com.mikepenz.markdown.compose.elements.MarkdownTableBasicText(
                        content = content,
                        cell = cell,
                        style = style,
                        maxLines = Int.MAX_VALUE,
                    )
                }
            }
        }
        HorizontalDivider()
    }

    @Composable
    private fun InfoBottomSheetContent(
        title: String,
        description: String,
        onDismiss: () -> Unit,
    ) {
        Column(
            modifier =
                Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.headlineSmall,
            )

            Markdown(
                modifier =
                    Modifier
                        .fillMaxWidth()
                        .verticalScroll(rememberScrollState()),
                content = description,
                typography =
                    markdownTypography(
                        h1 = MaterialTheme.typography.headlineSmall.copy(fontSize = 22.sp),
                        h2 = MaterialTheme.typography.titleLarge.copy(fontSize = 20.sp),
                        h3 = MaterialTheme.typography.titleMedium.copy(fontSize = 18.sp),
                        h4 = MaterialTheme.typography.titleMedium.copy(fontSize = 16.sp),
                        paragraph = MaterialTheme.typography.bodyMedium.copy(fontSize = 14.sp),
                        bullet = MaterialTheme.typography.bodyMedium.copy(fontSize = 14.sp),
                        ordered = MaterialTheme.typography.bodyMedium.copy(fontSize = 14.sp),
                        quote = MaterialTheme.typography.bodyMedium.copy(fontSize = 14.sp),
                        code = MaterialTheme.typography.bodyMedium.copy(fontFamily = FontFamily.Monospace, fontSize = 13.sp),
                        inlineCode = MaterialTheme.typography.bodyMedium.copy(fontFamily = FontFamily.Monospace, fontSize = 13.sp),
                        table = MaterialTheme.typography.bodyMedium.copy(fontSize = 14.sp),
                    ),
                components =
                    markdownComponents(
                        table = { model ->
                            MarkdownTable(
                                content = model.content,
                                node = model.node,
                                style = model.typography.table,
                                headerBlock = { content, header, _, style ->
                                    WrappedMarkdownTableRow(
                                        content = content,
                                        row = header,
                                        style = style.copy(fontWeight = FontWeight.Bold),
                                        isHeader = true,
                                    )
                                },
                                rowBlock = { content, row, _, style ->
                                    WrappedMarkdownTableRow(
                                        content = content,
                                        row = row,
                                        style = style,
                                        isHeader = false,
                                    )
                                },
                            )
                        },
                    ),
            )

            Button(
                onClick = onDismiss,
                modifier = Modifier.fillMaxWidth(),
            ) {
                Text("Понятно")
            }
        }
    }

    private companion object {
        private const val INFO_TITLE = "Информация о режиме работы"

        private val INFO_DESCRIPTION =
            $$$"""# Режим генерации документов

Этот режим предназначен для **массовой генерации документов** на основе данных из таблицы.
Кратко: программа берёт значения из таблицы и подставляет их в шаблонный `docx`-файл по заданным ключам.

<br>

## Параметры

| Параметр | Назначение |
|----------|------------|
| **Таблица с данными** | Источник значений, которые будут вставлены в шаблон |
| **Папка с шаблонами** | Содержит файлы `шаблон.docx` и `маски.txt`, по которым формируется итоговый документ |
| **Папка для сохранения результата** | Куда будет записан результат работы программы |

<br>
<br>

## Подробности о таблице, шаблонах и масках

### 1. Таблица с данными

- Таблица должна состоять **минимум из одного листа** — данные берутся именно с **первого листа**.
- В **первой колонке** указывается тип шаблона, который нужно использовать для данной строки.
- Далее располагаются данные для подстановки.
- **Пустые ячейки между значениями игнорируются**.
- Через интерфейс программы можно скрыть или объединить несколько последних колонок: сначала удаляются игнорируемые колонки, затем выполняется объединение.

### 2. Файл шаблона

- Должен называться **`шаблон.docx`**.
- Это заготовка документа, в которую подставляются данные.
- Структура документа не важна, но ключи должны быть **уникальными**.
- Важно: **один ключ не должен содержать в себе название другого ключа**, иначе подстановка будет выполнена неправильно.

### 3. Файл масок

- Должен называться **`маски.txt`**.
- Внутри перечисляются ключи через символ **`;`**.
- Рекомендуется окружать ключи символами `$$` и не использовать пробелы, например:

```text
$$key1$$;$$Key2$$;$$key_3$$;$$filename$$
```

- Специальный ключ `$$filename$$` используется для именования сгенерированного файла.
- Если `$$filename$$` не указан, файлы будут называться `dstFile1.docx`, `dstFile2.docx` и т.д.

<br>

## Структура папки с шаблонами

Файлы удобнее всего располагать следующим образом:

```text
папка с шаблонами/
├── шаблон1/
│   ├── маски.txt
│   └── шаблон.docx
├── шаблон2/
│   ├── маски.txt
│   └── шаблон.docx
└── таблица.xlsx
```

<br>

## Папка с результатом

После выбора папки назначения программа создаёт в ней папку **`generated`**.
Внутри неё для каждого шаблона создаётся отдельная подпапка, в которой и будут расположены сгенерированные файлы.
            """.trimIndent()
    }
}

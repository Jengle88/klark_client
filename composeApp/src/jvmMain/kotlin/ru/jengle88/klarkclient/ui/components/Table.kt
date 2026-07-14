package ru.jengle88.klarkclient.ui.components

import androidx.compose.desktop.ui.tooling.preview.Preview
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.persistentListOf

@Composable
fun TableView(data: ImmutableList<ImmutableList<String>>) {
    if (data.isEmpty()) return
    if (data.all { it.isEmpty() }) return
    val maxColumns = data.maxOf { it.size }

    val horizontalScrollState = rememberScrollState()

    Box(modifier = Modifier.fillMaxSize().horizontalScroll(horizontalScrollState)) {
        LazyColumn(
            modifier = Modifier.widthIn(min = 100.dp),
        ) {
            stickyHeader {
                Row(
                    modifier = Modifier.height(IntrinsicSize.Min),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    TableEmptyCell(background = MaterialTheme.colorScheme.secondaryContainer)
                    repeat(maxColumns) { i ->
                        TableLayoutCell(getColumnName(i))
                    }
                }
            }
            itemsIndexed(data) { index, row ->
                Row(
                    modifier = Modifier.height(IntrinsicSize.Min),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    TableLayoutCell((index + 1).toString())
                    row.forEach { cellText ->
                        if (cellText.isNotEmpty()) {
                            TableCell(text = cellText)
                        } else {
                            TableEmptyCell()
                        }
                    }
                    repeat(maxColumns - row.size) {
                        TableEmptyCell()
                    }
                }
            }
        }
    }
}

@Composable
private fun TableCell(text: String) {
    Text(
        text = text,
        modifier =
            Modifier
                .width(100.dp)
                .fillMaxSize()
                .border(1.dp, Color.LightGray)
                .padding(8.dp),
        textAlign = TextAlign.Start,
        maxLines = 3,
    )
}

@Composable
private fun TableLayoutCell(text: String) {
    Text(
        modifier =
            Modifier
                .width(100.dp)
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.secondaryContainer)
                .border(1.dp, Color.LightGray)
                .padding(8.dp),
        text = text,
        textAlign = TextAlign.Center,
    )
}

@Composable
private fun TableEmptyCell(background: Color = Color.Transparent) {
    Box(
        modifier =
            Modifier
                .width(100.dp)
                .fillMaxSize()
                .background(background)
                .border(1.dp, Color.LightGray)
                .padding(8.dp),
    )
}

private fun getColumnName(index: Int): String {
    val sb = StringBuilder()
    var num = index

    while (num >= 0) {
        val remainder = num % 26
        sb.append(('A' + remainder)) // 'A' + 0 = 'A', 'A' + 1 = 'B'...

        num = (num / 26) - 1
    }

    return sb.reverse().toString()
}

@Preview
@Composable
fun PreviewTableView() {
    val data =
        persistentListOf(
            persistentListOf("Заголовок 1", "Заголовок 2", "Заголовок 3", "Заголовок 4"),
            persistentListOf("Данные 1.1", "Данные 1.2", "Данные 1.3", ""),
            persistentListOf("Данные 2.1", "Данные 2.2", "Данные 2.3", "Данные 2.4"),
            persistentListOf("Данные 3.1", "", "Данные 3.3", "Данные 3.4"),
            persistentListOf("Данные 4.1", "Данные 4.2", "Данные 4.3", "dghskadfjlkjassbkhasd fasdkg asdg askhdg namsdg a"),
        )

    TableView(data)
}

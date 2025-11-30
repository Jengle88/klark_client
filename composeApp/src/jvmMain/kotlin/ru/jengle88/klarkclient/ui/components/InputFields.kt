package ru.jengle88.klarkclient.ui.components

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.FolderOpen
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType

@Composable
fun PathInputField(
    label: String,
    path: String,
    onPathChange: (String) -> Unit,
    onBrowseClick: () -> Unit,
) {
    OutlinedTextField(
        value = path,
        onValueChange = onPathChange,
        label = { Text(label) },
        modifier = Modifier.fillMaxWidth(),
        singleLine = true,
        trailingIcon = {
            IconButton(onClick = onBrowseClick) {
                Icon(Icons.Default.FolderOpen, contentDescription = "Выбрать файл")
            }
        },
    )
}

@Composable
fun NumberInputField(
    label: String,
    value: Int?,
    onValueChange: (Int?) -> Unit,
) {
    OutlinedTextField(
        value = value?.toString() ?: "",
        onValueChange = { text ->
            onValueChange(text.toIntOrNull())
        },
        label = { Text(label) },
        modifier = Modifier.fillMaxWidth(),
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
        singleLine = true,
    )
}

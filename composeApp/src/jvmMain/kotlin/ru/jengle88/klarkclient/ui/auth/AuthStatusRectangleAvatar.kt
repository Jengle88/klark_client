package ru.jengle88.klarkclient.ui.auth

import org.jetbrains.compose.ui.tooling.preview.Preview
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Person
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun AuthStatusRectangleAvatar(
    isAuthorized: Boolean,
    userName: String?,
    isLoading: Boolean,
    modifier: Modifier = Modifier,
    size: Dp = 48.dp,
    onClick: () -> Unit = {}
) {
    val shape = RoundedCornerShape(16.dp)

    Box(
        modifier = modifier
            .size(size)
            .clickable(enabled = !isLoading, onClick = onClick, indication = null, interactionSource = null),
        contentAlignment = Alignment.Center
    ) {
        if (isLoading) {
            CircularProgressIndicator(
                modifier = Modifier.size(size * 0.7f),
                strokeWidth = 2.dp,
                color = MaterialTheme.colorScheme.primary
            )
        } else if (isAuthorized) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(MaterialTheme.colorScheme.primary, shape),
                contentAlignment = Alignment.Center
            ) {
                if (userName != null) {
                    Text(
                        text = userName,
                        color = MaterialTheme.colorScheme.onPrimary,
                        fontWeight = FontWeight.Bold,
                        fontSize = (size.value / 2.5).sp
                    )
                } else {
                    Icon(
                        imageVector = Icons.Rounded.Person,
                        tint = MaterialTheme.colorScheme.onPrimary,
                        contentDescription = null,
                        modifier = Modifier.fillMaxSize(0.5f)
                    )
                }
            }
        } else {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(MaterialTheme.colorScheme.primaryContainer, shape),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Rounded.Person,
                    contentDescription = "Login",
                    tint = Color.DarkGray,
                    modifier = Modifier.fillMaxSize(0.5f)
                )
            }
        }
    }
}

@Preview
@Composable
private fun PreviewAuthStatusCircleAvatar() {
    MaterialTheme {
        Row {
            // Состояние: не авторизован
            AuthStatusRectangleAvatar(isAuthorized = false, isLoading = false, userName = null)
            Spacer(Modifier.width(8.dp))

            // Состояние: загрузка
            AuthStatusRectangleAvatar(isAuthorized = false, isLoading = true, userName = null)
            Spacer(Modifier.width(8.dp))

            // Состояние: авторизован с именем
            AuthStatusRectangleAvatar(isAuthorized = true, isLoading = false, userName = "UN")
            Spacer(Modifier.width(8.dp))

            // Состояние: авторизован без имени
            AuthStatusRectangleAvatar(isAuthorized = true, isLoading = false, userName = null)
        }
    }
}
package ru.jengle88.klarkclient.ui.components

import androidx.compose.desktop.ui.tooling.preview.Preview
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.CircularProgressIndicator
import androidx.compose.material.Icon
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Person
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun AuthStatusCircleAvatar(
    isAuthorized: Boolean,
    userName: String?,
    isLoading: Boolean,
    modifier: Modifier = Modifier,
    size: Dp = 48.dp,
    onClick: () -> Unit = {},
) {
    val shape = CircleShape

    Box(
        modifier =
            modifier
                .size(size)
                .clip(shape)
                .clickable(enabled = !isLoading, onClick = onClick),
        contentAlignment = Alignment.Center,
    ) {
        if (isLoading) {
            CircularProgressIndicator(
                modifier = Modifier.size(size * 0.7f),
                strokeWidth = 2.dp,
                color = MaterialTheme.colors.primary,
            )
        } else if (isAuthorized) {
            Box(
                modifier =
                    Modifier
                        .fillMaxSize()
                        .background(MaterialTheme.colors.primary)
                        .border(2.dp, MaterialTheme.colors.primaryVariant, shape),
                contentAlignment = Alignment.Center,
            ) {
                if (userName != null) {
                    Text(
                        text = userName,
                        color = MaterialTheme.colors.onPrimary,
                        fontWeight = FontWeight.Bold,
                        fontSize = (size.value / 2.5).sp,
                    )
                } else {
                    Icon(
                        imageVector = Icons.Rounded.Person,
                        tint = MaterialTheme.colors.onPrimary,
                        contentDescription = null,
                        modifier = Modifier.fillMaxSize(0.5f),
                    )
                }
            }
        } else {
            Box(
                modifier =
                    Modifier
                        .fillMaxSize()
                        .background(Color.LightGray)
                        .border(1.dp, Color.Gray, shape),
                contentAlignment = Alignment.Center,
            ) {
                Icon(
                    imageVector = Icons.Rounded.Person,
                    contentDescription = "Login",
                    tint = Color.DarkGray,
                    modifier = Modifier.fillMaxSize(0.5f),
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
            AuthStatusCircleAvatar(isAuthorized = false, isLoading = false, userName = null)
            Spacer(Modifier.width(8.dp))

            // Состояние: загрузка
            AuthStatusCircleAvatar(isAuthorized = false, isLoading = true, userName = null)
            Spacer(Modifier.width(8.dp))

            // Состояние: авторизован с именем
            AuthStatusCircleAvatar(isAuthorized = true, isLoading = false, userName = "UN")
            Spacer(Modifier.width(8.dp))

            // Состояние: авторизован без имени
            AuthStatusCircleAvatar(isAuthorized = true, isLoading = false, userName = null)
        }
    }
}

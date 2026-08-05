package ru.jengle88.klarkclient.ui.maintab

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.desktop.ui.tooling.preview.Preview
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.HelpOutline
import androidx.compose.material.icons.filled.AccountBalance
import androidx.compose.material.icons.filled.Calculate
import androidx.compose.material.icons.filled.TextFields
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.toImmutableList
import ru.jengle88.klarkclient.domain.api.update.UpdateInfo
import ru.jengle88.klarkclient.ui.datamodels.AppFeatureState
import ru.jengle88.klarkclient.ui.datamodels.AppScreenDestination

@Composable
fun MainTabContent(
    features: ImmutableList<AppFeatureState>,
    onNavigate: (AppScreenDestination) -> Unit,
    updateInfo: UpdateInfo?,
    onUpdateClick: () -> Unit,
) {
    Column(modifier = Modifier.fillMaxSize()) {
        LazyVerticalGrid(
            columns = GridCells.Adaptive(minSize = 160.dp),
            contentPadding = PaddingValues(16.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
            modifier = Modifier.weight(1f),
        ) {
            item(span = { GridItemSpan(maxLineSpan) }) {
                WelcomeCard()
            }

            // Список действий приложения
            items(features) { feature ->
                FeatureCard(
                    feature = feature,
                    onClick = { onNavigate(feature.route) },
                )
            }

            item {
                ComingSoonCard()
            }
        }

        AnimatedVisibility(visible = updateInfo != null) {
            updateInfo?.let { info ->
                UpdateBanner(
                    updateInfo = info,
                    onUpdateClick = onUpdateClick,
                )
            }
        }
    }
}

// --- Components ---

@Composable
fun WelcomeCard() {
    Card(
        colors =
        CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.secondaryContainer,
        ),
        modifier =
        Modifier
            .fillMaxWidth()
            .padding(bottom = 16.dp),
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
        ) {
            Text(
                text = "Добро пожаловать, Коллега!",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "Выберите инструмент для работы с задолженностями и документами.",
                style = MaterialTheme.typography.bodyLarge,
            )
        }
    }
}

@Composable
fun ComingSoonCard() {
    MainTabCard(
        icon = {
            Icon(
                imageVector = Icons.AutoMirrored.Filled.HelpOutline,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onPrimary,
            )
        },
        iconBackgroundColor = MaterialTheme.colorScheme.outline,
        title = "Новые фичи появятся позже",
        description = "Следите за обновлениями.",
        onClick = null,
    )
}

@Composable
fun FeatureCard(feature: AppFeatureState, onClick: () -> Unit) {
    MainTabCard(
        icon = {
            Icon(
                imageVector = feature.icon,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onPrimary,
            )
        },
        iconBackgroundColor = MaterialTheme.colorScheme.primary,
        title = feature.title,
        description = feature.description,
        onClick = onClick,
    )
}

@Composable
private fun MainTabCard(
    icon: @Composable () -> Unit,
    iconBackgroundColor: androidx.compose.ui.graphics.Color,
    title: String,
    description: String,
    onClick: (() -> Unit)?,
) {
    val cardModifier =
        Modifier
            .fillMaxWidth()
            .height(200.dp)
            .clip(RoundedCornerShape(12.dp))

    val clickableModifier =
        if (onClick != null) {
            cardModifier.clickable(onClick = onClick)
        } else {
            cardModifier
        }

    Card(
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        colors =
        CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface,
        ),
        modifier = clickableModifier,
    ) {
        Column(
            modifier =
            Modifier
                .padding(16.dp)
                .fillMaxSize(),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            // Иконка в кружочке
            Box(
                modifier =
                Modifier
                    .size(48.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(iconBackgroundColor),
                contentAlignment = Alignment.Center,
            ) {
                icon()
            }

            Column {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = description,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 3,
                    overflow = TextOverflow.Ellipsis,
                    lineHeight = 16.sp,
                )
            }
        }
    }
}

@Preview
@Composable
fun PreviewMainTabContent() {
    val sampleFeatures =
        listOf(
            AppFeatureState(
                title = "Расчет задолженности",
                description = "Калькулятор для расчета сумм задолженностей и неустоек.",
                icon = Icons.Default.Calculate,
                route = AppScreenDestination.DEBT_CALCULATOR,
            ),
            AppFeatureState(
                title = "Определение подсудности",
                description = "Помощник для определения подсудности спора.",
                icon = Icons.Default.AccountBalance,
                route = AppScreenDestination.JURISDICTION,
            ),
            AppFeatureState(
                title = "Текстовые утилиты",
                description = "Склонение ФИО, определение пола, числа прописью.",
                icon = Icons.Default.TextFields,
                route = AppScreenDestination.TEXT_UTILS,
            ),
        ).toImmutableList()

    MainTabContent(
        features = sampleFeatures,
        onNavigate = {},
        updateInfo = null,
        onUpdateClick = {},
    )
}

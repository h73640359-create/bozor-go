package uz.bozorgo.ui.screens.settings

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.ArrowBack
import androidx.compose.material.icons.outlined.DarkMode
import androidx.compose.material.icons.outlined.Language
import androidx.compose.material.icons.outlined.Notifications
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import kotlinx.coroutines.launch
import uz.bozorgo.R
import uz.bozorgo.ui.theme.BozorGoColors

@Composable
fun SettingsRoute(
    onBackClick: () -> Unit,
    snackbarHostState: SnackbarHostState,
    modifier: Modifier = Modifier,
    viewModel: SettingsViewModel = hiltViewModel(),
) {
    val state by viewModel.uiState.collectAsState()
    val scope = rememberCoroutineScope()
    val clearedMessage = stringResource(R.string.settings_history_cleared)
    val restartMessage = stringResource(R.string.settings_language_restart)

    SettingsScreen(
        state = state,
        modifier = modifier,
        onBackClick = onBackClick,
        onLanguageSelect = { code ->
            viewModel.setLanguage(code)
            scope.launch { snackbarHostState.showSnackbar(restartMessage) }
        },
        onThemeSelect = viewModel::setThemeMode,
        onNotificationsChange = viewModel::setNotificationsEnabled,
        onClearHistory = {
            viewModel.clearSearchHistory()
            scope.launch { snackbarHostState.showSnackbar(clearedMessage) }
        },
        onClearCacheRequest = viewModel::requestClearCache,
        onClearCacheConfirm = viewModel::clearCache,
        onClearCacheDismiss = viewModel::dismissClearCache,
        onReseed = viewModel::reseedCatalog,
    )
}

@Composable
private fun SettingsScreen(
    state: SettingsUiState,
    onBackClick: () -> Unit,
    onLanguageSelect: (String) -> Unit,
    onThemeSelect: (String) -> Unit,
    onNotificationsChange: (Boolean) -> Unit,
    onClearHistory: () -> Unit,
    onClearCacheRequest: () -> Unit,
    onClearCacheConfirm: () -> Unit,
    onClearCacheDismiss: () -> Unit,
    onReseed: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(modifier = modifier.fillMaxSize()) {
        Surface(shadowElevation = 1.dp, color = Color.White) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 6.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                IconButton(onClick = onBackClick) {
                    Icon(
                        Icons.Outlined.ArrowBack,
                        contentDescription = stringResource(R.string.common_back),
                    )
                }
                Text(
                    text = stringResource(R.string.settings_title),
                    style = MaterialTheme.typography.titleMedium,
                )
            }
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(12.dp),
        ) {
            GroupCard(title = stringResource(R.string.settings_language)) {
                LanguageOption(
                    title = "O'zbekcha (lotin)",
                    selected = state.language == "uz",
                    onSelect = { onLanguageSelect("uz") },
                )
                LanguageOption(
                    title = "Русский",
                    selected = state.language == "ru",
                    onSelect = { onLanguageSelect("ru") },
                )
                LanguageOption(
                    title = stringResource(R.string.settings_language_system),
                    selected = state.language.isBlank(),
                    onSelect = { onLanguageSelect("") },
                )
            }

            Spacer(Modifier.height(12.dp))

            GroupCard(title = stringResource(R.string.settings_theme)) {
                LanguageOption(
                    title = stringResource(R.string.settings_theme_system),
                    selected = state.themeMode == "system",
                    onSelect = { onThemeSelect("system") },
                )
                LanguageOption(
                    title = stringResource(R.string.settings_theme_light),
                    selected = state.themeMode == "light",
                    onSelect = { onThemeSelect("light") },
                )
                LanguageOption(
                    title = stringResource(R.string.settings_theme_dark),
                    selected = state.themeMode == "dark",
                    onSelect = { onThemeSelect("dark") },
                )
            }

            Spacer(Modifier.height(12.dp))

            GroupCard(title = stringResource(R.string.settings_data)) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 14.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text(
                        text = stringResource(R.string.profile_notifications),
                        style = MaterialTheme.typography.bodyMedium,
                        modifier = Modifier.weight(1f),
                    )
                    Switch(
                        checked = state.notificationsEnabled,
                        onCheckedChange = onNotificationsChange,
                    )
                }
                ActionRow(
                    title = stringResource(R.string.settings_clear_history),
                    onClick = onClearHistory,
                )
                ActionRow(
                    title = stringResource(R.string.settings_clear_cache),
                    subtitle = state.cacheSize,
                    onClick = onClearCacheRequest,
                )
                ActionRow(
                    title = stringResource(R.string.settings_reseed),
                    subtitle = stringResource(R.string.settings_reseed_hint, state.catalogCount),
                    onClick = onReseed,
                )
            }

            Spacer(Modifier.height(12.dp))

            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Text(
                        text = stringResource(R.string.settings_about_title),
                        style = MaterialTheme.typography.titleSmall,
                    )
                    Spacer(Modifier.height(6.dp))
                    KeyValue(
                        stringResource(R.string.settings_version),
                        "${state.versionName} (${state.versionCode})",
                    )
                    KeyValue(
                        stringResource(R.string.settings_products),
                        state.catalogCount.toString(),
                    )
                    KeyValue(
                        stringResource(R.string.settings_server),
                        if (state.isServerConfigured) {
                            stringResource(R.string.settings_server_connected)
                        } else {
                            stringResource(R.string.settings_server_offline)
                        },
                    )
                }
            }

            Spacer(Modifier.height(24.dp))
        }
    }

    if (state.showClearCacheDialog) {
        AlertDialog(
            onDismissRequest = onClearCacheDismiss,
            title = { Text(stringResource(R.string.settings_clear_cache)) },
            text = { Text(stringResource(R.string.settings_clear_cache_confirm)) },
            confirmButton = {
                TextButton(onClick = onClearCacheConfirm) {
                    Text(stringResource(R.string.common_yes))
                }
            },
            dismissButton = {
                TextButton(onClick = onClearCacheDismiss) {
                    Text(stringResource(R.string.common_no))
                }
            },
        )
    }
}

@Composable
private fun GroupCard(
    title: String,
    content: @Composable () -> Unit,
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
    ) {
        Column(modifier = Modifier.padding(vertical = 10.dp)) {
            Text(
                text = title,
                style = MaterialTheme.typography.labelLarge,
                color = BozorGoColors.TextSecondary,
                modifier = Modifier.padding(horizontal = 14.dp, vertical = 6.dp),
            )
            content()
        }
    }
}

@Composable
private fun LanguageOption(
    title: String,
    selected: Boolean,
    onSelect: () -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onSelect)
            .padding(horizontal = 8.dp, vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        RadioButton(selected = selected, onClick = onSelect)
        Spacer(Modifier.width(6.dp))
        Text(text = title, style = MaterialTheme.typography.bodyMedium)
    }
}

@Composable
private fun ActionRow(
    title: String,
    subtitle: String? = null,
    onClick: () -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(horizontal = 14.dp, vertical = 10.dp),
    ) {
        Text(text = title, style = MaterialTheme.typography.bodyMedium)
        if (!subtitle.isNullOrBlank()) {
            Text(
                text = subtitle,
                style = MaterialTheme.typography.labelSmall,
                color = BozorGoColors.TextTertiary,
            )
        }
    }
}

@Composable
private fun KeyValue(key: String, value: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 3.dp),
    ) {
        Text(
            text = key,
            style = MaterialTheme.typography.bodySmall,
            color = BozorGoColors.TextSecondary,
            modifier = Modifier.weight(1f),
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodyMedium,
            color = BozorGoColors.TextPrimary,
        )
    }
}

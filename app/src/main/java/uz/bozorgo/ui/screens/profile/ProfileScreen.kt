package uz.bozorgo.ui.screens.profile

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Description
import androidx.compose.material.icons.outlined.Edit
import androidx.compose.material.icons.outlined.Favorite
import androidx.compose.material.icons.outlined.HelpOutline
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material.icons.outlined.Language
import androidx.compose.material.icons.outlined.LocationOn
import androidx.compose.material.icons.outlined.Logout
import androidx.compose.material.icons.outlined.Notifications
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material.icons.outlined.PrivacyTip
import androidx.compose.material.icons.outlined.ReceiptLong
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import uz.bozorgo.R
import uz.bozorgo.ui.theme.BozorGoColors

@Composable
fun ProfileRoute(
    onLoginClick: () -> Unit,
    onEditProfile: () -> Unit,
    onOrdersClick: () -> Unit,
    onAddressesClick: () -> Unit,
    onFavoritesClick: () -> Unit,
    onHelpClick: () -> Unit,
    onPrivacyClick: () -> Unit,
    onTermsClick: () -> Unit,
    onSettingsClick: () -> Unit,
    contentPadding: PaddingValues = PaddingValues(0.dp),
    modifier: Modifier = Modifier,
    viewModel: ProfileViewModel = hiltViewModel(),
) {
    val state by viewModel.uiState.collectAsState()

    ProfileScreen(
        state = state,
        modifier = modifier.padding(contentPadding),

        onLoginClick = onLoginClick,
        onEditProfile = onEditProfile,
        onOrdersClick = onOrdersClick,
        onAddressesClick = onAddressesClick,
        onFavoritesClick = onFavoritesClick,
        onHelpClick = onHelpClick,
        onPrivacyClick = onPrivacyClick,
        onTermsClick = onTermsClick,
        onSettingsClick = onSettingsClick,
        onNotificationsChange = viewModel::setNotificationsEnabled,
        onLogoutRequest = viewModel::requestLogout,
        onLogoutConfirm = viewModel::logout,
        onLogoutDismiss = viewModel::dismissLogout,
    )
}

@Composable
private fun ProfileScreen(
    state: ProfileUiState,
    onLoginClick: () -> Unit,
    onEditProfile: () -> Unit,
    onOrdersClick: () -> Unit,
    onAddressesClick: () -> Unit,
    onFavoritesClick: () -> Unit,
    onHelpClick: () -> Unit,
    onPrivacyClick: () -> Unit,
    onTermsClick: () -> Unit,
    onSettingsClick: () -> Unit,
    onNotificationsChange: (Boolean) -> Unit,
    onLogoutRequest: () -> Unit,
    onLogoutConfirm: () -> Unit,
    onLogoutDismiss: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState()),
    ) {
        Surface(shadowElevation = 1.dp, color = Color.White) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = stringResource(R.string.profile_title),
                        style = MaterialTheme.typography.headlineSmall,
                        modifier = Modifier.weight(1f),
                    )
                    IconButton(onClick = onSettingsClick) {
                        Icon(
                            Icons.Outlined.Settings,
                            contentDescription = stringResource(R.string.settings_title),
                            tint = BozorGoColors.TextSecondary,
                        )
                    }
                }
                Spacer(Modifier.height(12.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(56.dp)
                            .padding(2.dp),
                        contentAlignment = Alignment.Center,
                    ) {
                        Surface(
                            shape = RoundedCornerShape(16.dp),
                            color = BozorGoColors.BrandGreenLight,
                            modifier = Modifier.fillMaxSize(),
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    Icons.Outlined.Person,
                                    contentDescription = null,
                                    tint = BozorGoColors.BrandGreen,
                                    modifier = Modifier.size(28.dp),
                                )
                            }
                        }
                    }
                    Spacer(Modifier.width(12.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = if (state.isLoggedIn) {
                                state.name.ifBlank { state.phone }
                            } else {
                                stringResource(R.string.profile_guest)
                            },
                            style = MaterialTheme.typography.titleMedium,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                        )
                        Text(
                            text = if (state.isLoggedIn) {
                                state.phone
                            } else {
                                stringResource(R.string.profile_guest_hint)
                            },
                            style = MaterialTheme.typography.bodySmall,
                            color = BozorGoColors.TextSecondary,
                        )
                    }
                    if (state.isLoggedIn) {
                        TextButton(onClick = onEditProfile) {
                            Text(stringResource(R.string.profile_edit))
                        }
                    }
                }
                if (!state.isLoggedIn) {
                    Spacer(Modifier.height(12.dp))
                    Button(
                        onClick = onLoginClick,
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(14.dp),
                    ) {
                        Text(stringResource(R.string.profile_login))
                    }
                }
            }
        }

        Spacer(Modifier.height(12.dp))

        MenuCard {
            MenuItem(
                icon = Icons.Outlined.ReceiptLong,
                title = stringResource(R.string.profile_orders),
                onClick = onOrdersClick,
            )
            MenuItem(
                icon = Icons.Outlined.LocationOn,
                title = stringResource(R.string.profile_addresses),
                onClick = onAddressesClick,
            )
            MenuItem(
                icon = Icons.Outlined.Favorite,
                title = stringResource(R.string.profile_favorites),
                onClick = onFavoritesClick,
            )
            SwitchMenuItem(
                icon = Icons.Outlined.Notifications,
                title = stringResource(R.string.profile_notifications),
                checked = state.notificationsEnabled,
                onCheckedChange = onNotificationsChange,
            )
            MenuItem(
                icon = Icons.Outlined.Language,
                title = stringResource(R.string.profile_language),
                subtitle = "O'zbekcha (lotin)",
                onClick = null,
            )
        }

        Spacer(Modifier.height(12.dp))

        MenuCard {
            MenuItem(
                icon = Icons.Outlined.HelpOutline,
                title = stringResource(R.string.profile_help),
                onClick = onHelpClick,
            )
            MenuItem(
                icon = Icons.Outlined.PrivacyTip,
                title = stringResource(R.string.profile_privacy),
                onClick = onPrivacyClick,
            )
            MenuItem(
                icon = Icons.Outlined.Description,
                title = stringResource(R.string.profile_terms),
                onClick = onTermsClick,
            )
            MenuItem(
                icon = Icons.Outlined.Info,
                title = stringResource(R.string.profile_about),
                subtitle = stringResource(R.string.profile_version, "1.0.0", 1) +
                    " • ${state.catalogCount} ta mahsulot",
                onClick = null,
            )
        }

        Spacer(Modifier.height(12.dp))

        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp),
            shape = RoundedCornerShape(14.dp),
            colors = CardDefaults.cardColors(containerColor = BozorGoColors.BrandOrangeLight),
        ) {
            Text(
                text = stringResource(R.string.profile_demo_notice),
                style = MaterialTheme.typography.bodySmall,
                color = BozorGoColors.TextPrimary,
                modifier = Modifier.padding(14.dp),
            )
        }

        if (state.isLoggedIn) {
            Spacer(Modifier.height(12.dp))
            MenuCard {
                MenuItem(
                    icon = Icons.Outlined.Logout,
                    title = stringResource(R.string.profile_logout),
                    titleColor = BozorGoColors.Error,
                    onClick = onLogoutRequest,
                )
            }
        }

        Spacer(Modifier.height(24.dp))
    }

    if (state.showLogoutDialog) {
        AlertDialog(
            onDismissRequest = onLogoutDismiss,
            title = { Text(stringResource(R.string.profile_logout)) },
            text = { Text(stringResource(R.string.profile_logout_confirm)) },
            confirmButton = {
                TextButton(onClick = onLogoutConfirm) { Text(stringResource(R.string.common_yes)) }
            },
            dismissButton = {
                TextButton(onClick = onLogoutDismiss) { Text(stringResource(R.string.common_no)) }
            },
        )
    }
}

@Composable
private fun MenuCard(content: @Composable () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 12.dp),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
    ) {
        Column(
            modifier = Modifier.padding(vertical = 4.dp),
            verticalArrangement = Arrangement.spacedBy(2.dp),
        ) {
            content()
        }
    }
}

@Composable
private fun MenuItem(
    icon: ImageVector,
    title: String,
    subtitle: String? = null,
    titleColor: Color = BozorGoColors.TextPrimary,
    onClick: (() -> Unit)?,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .then(if (onClick != null) Modifier.clickable(onClick = onClick) else Modifier)
            .padding(horizontal = 14.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = if (titleColor == BozorGoColors.TextPrimary) {
                BozorGoColors.TextSecondary
            } else {
                titleColor
            },
            modifier = Modifier.size(22.dp),
        )
        Spacer(Modifier.width(14.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(text = title, style = MaterialTheme.typography.bodyMedium, color = titleColor)
            if (!subtitle.isNullOrBlank()) {
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.labelSmall,
                    color = BozorGoColors.TextTertiary,
                )
            }
        }
    }
}

@Composable
private fun SwitchMenuItem(
    icon: ImageVector,
    title: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 14.dp, vertical = 6.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = BozorGoColors.TextSecondary,
            modifier = Modifier.size(22.dp),
        )
        Spacer(Modifier.width(14.dp))
        Text(
            text = title,
            style = MaterialTheme.typography.bodyMedium,
            modifier = Modifier.weight(1f),
        )
        Switch(checked = checked, onCheckedChange = onCheckedChange)
    }
}

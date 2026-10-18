package uz.bozorgo.ui.screens.auth

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.ArrowBack
import androidx.compose.material.icons.outlined.Phone
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import kotlinx.coroutines.launch
import uz.bozorgo.R
import uz.bozorgo.ui.theme.BozorGoColors

@Composable
fun AuthRoute(
    onBackClick: () -> Unit,
    onAuthenticated: () -> Unit,
    snackbarHostState: SnackbarHostState,
    modifier: Modifier = Modifier,
    viewModel: AuthViewModel = hiltViewModel(),
) {
    val state by viewModel.uiState.collectAsState()
    val scope = rememberCoroutineScope()
    val codeSentMessage = stringResource(R.string.auth_code_sent)

    LaunchedEffect(state.demoCode) {
        if (state.demoCode.isNotBlank()) {
            scope.launch { snackbarHostState.showSnackbar("$codeSentMessage: ${state.demoCode}") }
        }
    }

    LaunchedEffect(state.isAuthenticated) {
        if (state.isAuthenticated) onAuthenticated()
    }

    AuthScreen(
        state = state,
        modifier = modifier,
        onBackClick = onBackClick,
        onPhoneChange = viewModel::onPhoneChange,
        onCodeChange = viewModel::onCodeChange,
        onNameChange = viewModel::onNameChange,
        onSendCode = viewModel::requestCode,
        onVerify = viewModel::verify,
        onChangePhone = viewModel::resetToPhoneStep,
    )
}

@Composable
private fun AuthScreen(
    state: AuthUiState,
    onBackClick: () -> Unit,
    onPhoneChange: (String) -> Unit,
    onCodeChange: (String) -> Unit,
    onNameChange: (String) -> Unit,
    onSendCode: () -> Unit,
    onVerify: () -> Unit,
    onChangePhone: () -> Unit,
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
                    text = stringResource(R.string.auth_title),
                    style = MaterialTheme.typography.titleSmall,
                )
            }
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Top,
        ) {
            Spacer(Modifier.height(24.dp))
            Surface(
                shape = RoundedCornerShape(20.dp),
                color = BozorGoColors.BrandGreenLight,
                modifier = Modifier.size(76.dp),
            ) {
                androidx.compose.foundation.layout.Box(contentAlignment = Alignment.Center) {
                    Icon(
                        Icons.Outlined.Phone,
                        contentDescription = null,
                        tint = BozorGoColors.BrandGreen,
                        modifier = Modifier.size(34.dp),
                    )
                }
            }
            Spacer(Modifier.height(20.dp))
            Text(
                text = if (state.step == AuthStep.PHONE) {
                    stringResource(R.string.auth_phone_label)
                } else {
                    stringResource(R.string.auth_code_label)
                },
                style = MaterialTheme.typography.titleMedium,
            )
            Spacer(Modifier.height(6.dp))

            if (state.step == AuthStep.PHONE) {
                OutlinedTextField(
                    value = state.phone,
                    onValueChange = onPhoneChange,
                    modifier = Modifier.fillMaxWidth(),
                    label = { Text(stringResource(R.string.checkout_phone_hint)) },
                    singleLine = true,
                    keyboardOptions = androidx.compose.ui.text.input.KeyboardOptions(
                        keyboardType = KeyboardType.Phone,
                    ),
                    shape = RoundedCornerShape(14.dp),
                    isError = state.error != null,
                )
                Spacer(Modifier.height(8.dp))
                OutlinedTextField(
                    value = state.name,
                    onValueChange = onNameChange,
                    modifier = Modifier.fillMaxWidth(),
                    label = { Text(stringResource(R.string.checkout_name_hint)) },
                    singleLine = true,
                    shape = RoundedCornerShape(14.dp),
                )
            } else {
                OutlinedTextField(
                    value = state.code,
                    onValueChange = onCodeChange,
                    modifier = Modifier.fillMaxWidth(),
                    label = { Text(stringResource(R.string.auth_code_label)) },
                    singleLine = true,
                    keyboardOptions = androidx.compose.ui.text.input.KeyboardOptions(
                        keyboardType = KeyboardType.Number,
                    ),
                    shape = RoundedCornerShape(14.dp),
                    isError = state.error != null,
                )
                Spacer(Modifier.height(8.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = state.phone,
                        style = MaterialTheme.typography.bodyMedium,
                        modifier = Modifier.weight(1f),
                    )
                    androidx.compose.material3.TextButton(onClick = onChangePhone) {
                        Text(stringResource(R.string.common_edit))
                    }
                }
            }

            if (state.error != null) {
                Spacer(Modifier.height(8.dp))
                Text(
                    text = state.error,
                    style = MaterialTheme.typography.bodySmall,
                    color = BozorGoColors.Error,
                )
            }

            Spacer(Modifier.height(20.dp))
            Button(
                onClick = if (state.step == AuthStep.PHONE) onSendCode else onVerify,
                enabled = !state.isLoading,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp),
                shape = RoundedCornerShape(14.dp),
            ) {
                Text(
                    text = if (state.isLoading) {
                        stringResource(R.string.common_loading)
                    } else if (state.step == AuthStep.PHONE) {
                        stringResource(R.string.auth_send_code)
                    } else {
                        stringResource(R.string.auth_verify)
                    },
                )
            }

            Spacer(Modifier.height(16.dp))
            Surface(
                shape = RoundedCornerShape(12.dp),
                color = BozorGoColors.BrandOrangeLight,
                modifier = Modifier.fillMaxWidth(),
            ) {
                Text(
                    text = stringResource(R.string.auth_demo_warning),
                    style = MaterialTheme.typography.labelSmall,
                    color = BozorGoColors.TextPrimary,
                    modifier = Modifier.padding(12.dp),
                )
            }
        }
    }
}

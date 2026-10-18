package uz.bozorgo.ui.screens.address

import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Add
import androidx.compose.material.icons.outlined.ArrowBack
import androidx.compose.material.icons.outlined.DeleteOutline
import androidx.compose.material.icons.outlined.LocationOn
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import kotlinx.coroutines.launch
import uz.bozorgo.R
import uz.bozorgo.domain.model.Address
import uz.bozorgo.ui.components.EmptyState
import uz.bozorgo.ui.theme.BozorGoColors
import java.util.UUID

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddressesRoute(
    onBackClick: () -> Unit,
    snackbarHostState: SnackbarHostState,
    modifier: Modifier = Modifier,
    viewModel: AddressesViewModel = hiltViewModel(),
) {
    val state by viewModel.uiState.collectAsState()
    val sheetState = rememberModalBottomSheetState()
    val scope = rememberCoroutineScope()
    var showEditor by remember { mutableStateOf(false) }
    var editingId by remember { mutableStateOf<String?>(null) }

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
                    text = stringResource(R.string.address_title),
                    style = MaterialTheme.typography.titleMedium,
                    modifier = Modifier.weight(1f),
                )
                IconButton(onClick = {
                    editingId = null
                    showEditor = true
                }) {
                    Icon(Icons.Outlined.Add, contentDescription = stringResource(R.string.address_add))
                }
            }
        }

        if (state.addresses.isEmpty()) {
            EmptyState(
                title = stringResource(R.string.address_empty),
                subtitle = stringResource(R.string.address_add),
                actionLabel = stringResource(R.string.address_add),
                onAction = {
                    editingId = null
                    showEditor = true
                },
            )
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = androidx.compose.foundation.layout.PaddingValues(12.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp),
            ) {
                items(state.addresses, key = { it.id }) { address ->
                    AddressCard(
                        address = address,
                        onDelete = { viewModel.delete(address.id) },
                        onSelectAsDefault = { viewModel.makeDefault(address.id) },
                        onEdit = {
                            editingId = address.id
                            showEditor = true
                        },
                    )
                }
            }
        }
    }

    if (showEditor) {
        val existing = state.addresses.firstOrNull { it.id == editingId }
        ModalBottomSheet(
            onDismissRequest = { showEditor = false },
            sheetState = sheetState,
        ) {
            AddressEditor(
                address = existing,
                onSave = { address ->
                    viewModel.save(address)
                    scope.launch { sheetState.hide() }.invokeOnCompletion { showEditor = false }
                },
                onDismiss = { showEditor = false },
            )
        }
    }
}

@Composable
private fun AddressCard(
    address: Address,
    onDelete: () -> Unit,
    onSelectAsDefault: () -> Unit,
    onEdit: () -> Unit,
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Icon(
                Icons.Outlined.LocationOn,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
            )
            Spacer(Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = address.title,
                        style = MaterialTheme.typography.titleSmall,
                        modifier = Modifier.weight(1f),
                    )
                    if (address.isDefault) {
                        Surface(
                            shape = RoundedCornerShape(6.dp),
                            color = BozorGoColors.BrandGreenLight,
                            contentColor = BozorGoColors.BrandGreenDark,
                        ) {
                            Text(
                                text = stringResource(R.string.address_default_badge),
                                style = MaterialTheme.typography.labelSmall,
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                            )
                        }
                    }
                }
                Spacer(Modifier.height(4.dp))
                Text(
                    text = address.fullAddress,
                    style = MaterialTheme.typography.bodySmall,
                    color = BozorGoColors.TextSecondary,
                )
                if (address.comment.isNotBlank()) {
                    Text(
                        text = address.comment,
                        style = MaterialTheme.typography.labelSmall,
                        color = BozorGoColors.TextTertiary,
                    )
                }
                Row(verticalAlignment = Alignment.CenterVertically) {
                    TextButton(onClick = onEdit) { Text(stringResource(R.string.common_edit)) }
                    if (!address.isDefault) {
                        TextButton(onClick = onSelectAsDefault) {
                            Text(stringResource(R.string.address_set_default))
                        }
                    }
                }
            }
            IconButton(onClick = onDelete) {
                Icon(
                    Icons.Outlined.DeleteOutline,
                    contentDescription = stringResource(R.string.common_delete),
                    tint = BozorGoColors.TextTertiary,
                )
            }
        }
    }
}

@Composable
private fun AddressEditor(
    address: Address?,
    onSave: (Address) -> Unit,
    onDismiss: () -> Unit,
) {
    var title by remember { mutableStateOf(address?.title.orEmpty()) }
    var city by remember { mutableStateOf(address?.city ?: "Toshkent") }
    var district by remember { mutableStateOf(address?.district.orEmpty()) }
    var street by remember { mutableStateOf(address?.street.orEmpty()) }
    var house by remember { mutableStateOf(address?.house.orEmpty()) }
    var apartment by remember { mutableStateOf(address?.apartment.orEmpty()) }
    var comment by remember { mutableStateOf(address?.comment.orEmpty()) }
    var isDefault by remember { mutableStateOf(address?.isDefault ?: false) }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp)
            .padding(bottom = 24.dp)
            .height(520.dp),
    ) {
        Text(
            text = if (address == null) {
                stringResource(R.string.address_add)
            } else {
                stringResource(R.string.common_edit)
            },
            style = MaterialTheme.typography.titleMedium,
        )
        Spacer(Modifier.height(12.dp))
        Field(title, { title = it }, stringResource(R.string.address_label))
        Field(city, { city = it }, stringResource(R.string.address_city))
        Field(district, { district = it }, stringResource(R.string.address_district))
        Field(street, { street = it }, stringResource(R.string.address_street))
        Field(house, { house = it }, stringResource(R.string.address_house))
        Field(apartment, { apartment = it }, stringResource(R.string.address_apartment))
        Field(comment, { comment = it }, stringResource(R.string.address_comment))
        Row(verticalAlignment = Alignment.CenterVertically) {
            Checkbox(checked = isDefault, onCheckedChange = { isDefault = it })
            Text(stringResource(R.string.address_set_default))
        }
        Spacer(Modifier.height(12.dp))
        Row(modifier = Modifier.fillMaxWidth()) {
            TextButton(onClick = onDismiss, modifier = Modifier.weight(1f)) {
                Text(stringResource(R.string.common_cancel))
            }
            Button(
                onClick = {
                    onSave(
                        Address(
                            id = address?.id ?: UUID.randomUUID().toString(),
                            title = title.ifBlank { "Uy" },
                            city = city,
                            district = district,
                            street = street,
                            house = house,
                            apartment = apartment,
                            comment = comment,
                            isDefault = isDefault,
                        ),
                    )
                },
                modifier = Modifier.weight(1f),
                enabled = street.isNotBlank() && house.isNotBlank(),
            ) {
                Text(stringResource(R.string.common_save))
            }
        }
    }
}

@Composable
private fun Field(
    value: String,
    onChange: (String) -> Unit,
    label: String,
) {
    OutlinedTextField(
        value = value,
        onValueChange = onChange,
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        label = { Text(label) },
        singleLine = true,
        shape = RoundedCornerShape(12.dp),
    )
}

@Composable
private fun DefaultSwitch(checked: Boolean, onCheckedChange: (Boolean) -> Unit) {
    Switch(checked = checked, onCheckedChange = onCheckedChange)
}

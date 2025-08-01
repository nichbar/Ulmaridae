package now.link.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import now.link.R
import now.link.model.AgentConfiguration

@Composable
fun ConfigurationDialog(
    configuration: AgentConfiguration,
    onDismiss: () -> Unit,
    onSave: (AgentConfiguration) -> Unit
) {
    var server by remember { mutableStateOf(configuration.server) }
    var secret by remember { mutableStateOf(configuration.secret) }
    var uuid by remember { mutableStateOf(configuration.uuid) }
    var enableCommandExecute by remember { mutableStateOf(configuration.enableCommandExecute) }
    var passwordVisible by remember { mutableStateOf(false) }
    
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(stringResource(id = R.string.configure_nezha_agent))
        },
        text = {
            Column(
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Server URL Field
                OutlinedTextField(
                    value = server,
                    onValueChange = { server = it },
                    label = { Text("Server URL") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Uri),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = MaterialTheme.colorScheme.primary,
                        focusedLabelColor = MaterialTheme.colorScheme.primary
                    )
                )
                
                // Agent Secret Field
                OutlinedTextField(
                    value = secret,
                    onValueChange = { secret = it },
                    label = { Text("Agent Secret") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                    trailingIcon = {
                        IconButton(onClick = { passwordVisible = !passwordVisible }) {
                            Icon(
                                painter = painterResource(
                                    id = if (passwordVisible) R.drawable.ic_visibility_off else R.drawable.ic_visibility
                                ),
                                contentDescription = null
                            )
                        }
                    },
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = MaterialTheme.colorScheme.primary,
                        focusedLabelColor = MaterialTheme.colorScheme.primary
                    )
                )
                
                // UUID Field
                OutlinedTextField(
                    value = uuid,
                    onValueChange = { uuid = it },
                    label = { Text("UUID (Optional)") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Text),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = MaterialTheme.colorScheme.primary,
                        focusedLabelColor = MaterialTheme.colorScheme.primary
                    )
                )
                
                // Enable Command Execute Switch
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = androidx.compose.ui.Alignment.CenterVertically
                ) {
                    Text(
                        text = stringResource(id = R.string.enable_command_execute),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Switch(
                        checked = enableCommandExecute,
                        onCheckedChange = { enableCommandExecute = it },
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = MaterialTheme.colorScheme.primary,
                            checkedTrackColor = MaterialTheme.colorScheme.primaryContainer
                        )
                    )
                }
                
                // Description Text
                Text(
                    text = stringResource(id = R.string.configuration_dialog_description),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(top = 8.dp)
                )
            }
        },
        confirmButton = {
            TextButton(
                onClick = {
                    if (server.isNotBlank() && secret.isNotBlank()) {
                        onSave(
                            AgentConfiguration(
                                server = server.trim(),
                                secret = secret.trim(),
                                uuid = uuid.trim(),
                                clientId = configuration.clientId,
                                enableTLS = configuration.enableTLS,
                                enableCommandExecute = enableCommandExecute
                            )
                        )
                    }
                }
            ) {
                Text(stringResource(id = R.string.save))
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(stringResource(id = R.string.cancel))
            }
        }
    )
}

@Composable
fun WakeLockInfoDialog(
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(stringResource(id = R.string.wake_lock_enabled))
        },
        text = {
            Text(stringResource(id = R.string.wake_lock_info))
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text(stringResource(id = R.string.got_it))
            }
        }
    )
}

@Composable
fun PermissionDeniedDialog(
    onOpenSettings: () -> Unit,
    onContinueAnyway: () -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(stringResource(id = R.string.permissions_required))
        },
        text = {
            Text(stringResource(id = R.string.permissions_message))
        },
        confirmButton = {
            TextButton(onClick = onOpenSettings) {
                Text(stringResource(id = R.string.settings))
            }
        },
        dismissButton = {
            Column {
                TextButton(onClick = onContinueAnyway) {
                    Text(stringResource(id = R.string.continue_anyway))
                }
                TextButton(onClick = onDismiss) {
                    Text(stringResource(id = R.string.cancel))
                }
            }
        }
    )
}

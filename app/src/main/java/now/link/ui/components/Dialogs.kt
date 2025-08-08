package now.link.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import now.link.R
import now.link.agent.AgentType
import now.link.agent.KomariAgentConfiguration
import now.link.agent.NezhaAgentConfiguration
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
                    text = stringResource(id = R.string.nezha_configuration_description),
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
fun UnifiedConfigurationDialog(
    currentAgentType: AgentType,
    configuration: now.link.agent.AgentConfiguration,
    onDismiss: () -> Unit,
    onSave: (AgentType, now.link.agent.AgentConfiguration) -> Unit,
) {
    var passwordVisible by remember { mutableStateOf(false) }

    // Common fields
    var server by remember { mutableStateOf(configuration.server) }
    var secret by remember { mutableStateOf(configuration.secret) }
    var uuid by remember { mutableStateOf(configuration.uuid) }
    var enableCommandExecute by remember { mutableStateOf(configuration.enableCommandExecute) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            when (currentAgentType) {
                AgentType.NEZHA_AGENT -> Text(stringResource(id = R.string.configure_nezha_agent))
                AgentType.KOMARI_AGENT -> Text(stringResource(id = R.string.configure_komari_agent))
            }
        },
        text = {
            Column(
                modifier = Modifier
                    .verticalScroll(rememberScrollState())
                    .fillMaxWidth(),
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

                OutlinedTextField(
                    value = secret,
                    onValueChange = { secret = it },
                    label = {
                        Text(
                            when (currentAgentType) {
                                AgentType.NEZHA_AGENT -> "Agent Secret"
                                AgentType.KOMARI_AGENT -> "Token"
                            }
                        )
                    },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                    trailingIcon = {
                        IconButton(onClick = { passwordVisible = !passwordVisible }) {
                            Icon(
                                painter = painterResource(
                                    id = if (passwordVisible) R.drawable.ic_visibility_off else R.drawable.ic_visibility
                                ),
                                contentDescription = if (passwordVisible) "Hide password" else "Show password"
                            )
                        }
                    },
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = MaterialTheme.colorScheme.primary,
                        focusedLabelColor = MaterialTheme.colorScheme.primary
                    )
                )

                if (currentAgentType == AgentType.NEZHA_AGENT) {
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
                }

                // Command Execute Toggle
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

                // Description
                Text(
                    text = when (currentAgentType) {
                        AgentType.NEZHA_AGENT -> stringResource(id = R.string.nezha_configuration_description)
                        AgentType.KOMARI_AGENT -> stringResource(id = R.string.komari_configuration_description)
                    },
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
                        val newConfiguration = when (currentAgentType) {
                            AgentType.NEZHA_AGENT -> NezhaAgentConfiguration(
                                server = server,
                                secret = secret,
                                uuid = uuid,
                                enableTLS = true,
                                enableCommandExecute = enableCommandExecute
                            )
                            AgentType.KOMARI_AGENT -> KomariAgentConfiguration(
                                server = server,
                                secret = secret,
                                uuid = uuid,
                                enableTLS = true,
                                enableCommandExecute = enableCommandExecute,
                            )
                        }
                        onSave(currentAgentType, newConfiguration)
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
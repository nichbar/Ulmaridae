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
import kotlin.collections.forEach

@Composable
fun UnifiedConfigurationDialog(
    currentAgentType: AgentType,
    configuration: now.link.agent.AgentConfiguration,
    availableAgentTypes: List<AgentType>,
    onDismiss: () -> Unit,
    onSave: (AgentType, now.link.agent.AgentConfiguration) -> Unit,
    onAgentTypeChanged: (AgentType) -> Unit
) {
    var selectedAgentType by remember { mutableStateOf(currentAgentType) }
    var passwordVisible by remember { mutableStateOf(false) }

    // Common fields
    var server by remember { mutableStateOf(configuration.server) }
    var secret by remember { mutableStateOf(configuration.secret) }
    var uuid by remember { mutableStateOf(configuration.uuid) }
    var enableCommandExecute by remember { mutableStateOf(configuration.enableCommandExecute) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text("Configure Monitoring Agent")
        },
        text = {
            Column(
                modifier = Modifier
                    .verticalScroll(rememberScrollState())
                    .fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Agent Type Selection
                if (availableAgentTypes.size > 1) {
                    Text(
                        text = "Agent Type",
                        style = MaterialTheme.typography.titleSmall
                    )

                    availableAgentTypes.forEach { agentType ->
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = agentType.displayName,
                                style = MaterialTheme.typography.bodyMedium
                            )
                            RadioButton(
                                selected = selectedAgentType == agentType,
                                onClick = {
                                    selectedAgentType = agentType
                                    onAgentTypeChanged(agentType)
                                }
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))
                }

                // Common Fields
                OutlinedTextField(
                    value = server,
                    onValueChange = { server = it },
                    label = { Text("Server URL") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Uri),
                    supportingText = {
                        when (selectedAgentType) {
                            AgentType.NEZHA_AGENT -> Text("e.g., your-server.com:443")
                            AgentType.KOMARI_AGENT -> Text("e.g., https://your-server.com")
                        }
                    }
                )

                OutlinedTextField(
                    value = secret,
                    onValueChange = { secret = it },
                    label = {
                        Text(
                            when (selectedAgentType) {
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
                    }
                )

                OutlinedTextField(
                    value = uuid,
                    onValueChange = { uuid = it },
                    label = { Text("UUID (Optional)") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    supportingText = { Text("Leave empty to auto-generate") }
                )

                // Command Execute Toggle
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = when (selectedAgentType) {
                            AgentType.NEZHA_AGENT -> stringResource(id = R.string.enable_command_execute)
                            AgentType.KOMARI_AGENT -> "Enable Web SSH"
                        },
                        style = MaterialTheme.typography.bodyMedium
                    )
                    Switch(
                        checked = enableCommandExecute,
                        onCheckedChange = { enableCommandExecute = it }
                    )
                }

                // Description
                Text(
                    text = when (selectedAgentType) {
                        AgentType.NEZHA_AGENT -> stringResource(id = R.string.nezha_configuration_description)
                        AgentType.KOMARI_AGENT -> stringResource(id = R.string.komari_configuration_description)
                    },
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        },
        confirmButton = {
            TextButton(
                onClick = {
                    if (server.isNotBlank() && secret.isNotBlank()) {
                        val newConfiguration = when (selectedAgentType) {
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
                        onSave(selectedAgentType, newConfiguration)
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
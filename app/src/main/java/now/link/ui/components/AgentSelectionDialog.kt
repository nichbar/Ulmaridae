package now.link.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import now.link.R
import now.link.agent.AgentType

@Composable
fun AgentSelectionDialog(
    onAgentSelected: (AgentType) -> Unit,
    onDismiss: () -> Unit = {}
) {
    var selectedAgent by remember { mutableStateOf(AgentType.NEZHA_AGENT) }

    Dialog(
        onDismissRequest = { /* Prevent dismissing on outside click for first launch */ },
        properties = DialogProperties(
            dismissOnBackPress = false,
            dismissOnClickOutside = false
        )
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            shape = RoundedCornerShape(16.dp),
            elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
        ) {
            Column(
                modifier = Modifier.padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Title
                Text(
                    text = stringResource(R.string.choose_monitoring_agent),
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Center
                )
                
                Spacer(modifier = Modifier.height(24.dp))

                // Agent selection options
                AgentType.entries.forEach { agentType ->
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .selectable(
                                selected = (selectedAgent == agentType),
                                onClick = { selectedAgent = agentType }
                            ),
                        colors = CardDefaults.cardColors(
                            containerColor = if (selectedAgent == agentType) {
                                MaterialTheme.colorScheme.primaryContainer
                            } else {
                                MaterialTheme.colorScheme.surface
                            }
                        ),
                        border = if (selectedAgent == agentType) {
                            CardDefaults.outlinedCardBorder().copy(width = 2.dp)
                        } else {
                            CardDefaults.outlinedCardBorder()
                        }
                    ) {
                        Column(
                            modifier = Modifier.padding(16.dp)
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                RadioButton(
                                    selected = (selectedAgent == agentType),
                                    onClick = { selectedAgent = agentType }
                                )
                                
                                Spacer(modifier = Modifier.width(8.dp))
                                
                                Text(
                                    text = when (agentType) {
                                        AgentType.NEZHA_AGENT -> stringResource(R.string.nezha_agent_name)
                                        AgentType.KOMARI_AGENT -> stringResource(R.string.komari_agent_name)
                                    },
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Medium
                                )
                            }
                        }
                    }
                    
                    if (agentType != AgentType.values().last()) {
                        Spacer(modifier = Modifier.height(8.dp))
                    }
                }
                
                Spacer(modifier = Modifier.height(24.dp))
                
                // Continue button
                Button(
                    onClick = { onAgentSelected(selectedAgent) },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(stringResource(R.string.continue_with_selection))
                }
            }
        }
    }
}

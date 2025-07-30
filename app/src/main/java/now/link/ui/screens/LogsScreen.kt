package now.link.ui.screens

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.widget.Toast
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.launch
import now.link.R
import now.link.model.LogEntry
import now.link.ui.theme.LogColors
import now.link.utils.LogManager

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LogsScreen(
    onBackClick: () -> Unit = {}
) {
    val context = LocalContext.current
    val logEntries by LogManager.logEntriesLiveData.observeAsState(emptyList())
    val listState = rememberLazyListState()
    val coroutineScope = rememberCoroutineScope()
    var showMenu by remember { mutableStateOf(false) }
    
    // Handle back press
    BackHandler {
        onBackClick()
    }
    
    // Auto-scroll to bottom when new logs arrive
    LaunchedEffect(logEntries.size) {
        if (logEntries.isNotEmpty()) {
            coroutineScope.launch {
                listState.scrollToItem(logEntries.size - 1)
            }
        }
    }
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(id = R.string.view_logs)) },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = stringResource(id = R.string.back)
                        )
                    }
                },
                actions = {
                    Box {
                        IconButton(onClick = { showMenu = true }) {
                            Icon(
                                imageVector = Icons.Default.MoreVert,
                                contentDescription = stringResource(id = R.string.menu)
                            )
                        }
                        DropdownMenu(
                            expanded = showMenu,
                            onDismissRequest = { showMenu = false }
                        ) {
                            DropdownMenuItem(
                                text = { Text(stringResource(id = R.string.scroll_to_top)) },
                                onClick = {
                                    showMenu = false
                                    coroutineScope.launch {
                                        if (logEntries.isNotEmpty()) {
                                            listState.animateScrollToItem(0)
                                        }
                                    }
                                }
                            )
                            DropdownMenuItem(
                                text = { Text(stringResource(id = R.string.scroll_to_bottom)) },
                                onClick = {
                                    showMenu = false
                                    coroutineScope.launch {
                                        if (logEntries.isNotEmpty()) {
                                            listState.animateScrollToItem(logEntries.size - 1)
                                        }
                                    }
                                }
                            )
                            DropdownMenuItem(
                                text = { Text(stringResource(id = R.string.copy_logs)) },
                                onClick = {
                                    showMenu = false
                                    copyLogsToClipboard(context, logEntries)
                                }
                            )
                            DropdownMenuItem(
                                text = { Text(stringResource(id = R.string.clear_logs)) },
                                onClick = {
                                    showMenu = false
                                    LogManager.clearLogs()
                                    Toast.makeText(
                                        context,
                                        context.getString(R.string.logs_cleared),
                                        Toast.LENGTH_SHORT
                                    ).show()
                                }
                            )
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background,
                    titleContentColor = MaterialTheme.colorScheme.onBackground,
                    navigationIconContentColor = MaterialTheme.colorScheme.onBackground,
                    actionIconContentColor = MaterialTheme.colorScheme.onBackground
                )
            )
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            if (logEntries.isEmpty()) {
                // Empty state
                EmptyLogsState()
            } else {
                // Logs list
                LazyColumn(
                    state = listState,
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(8.dp),
                    verticalArrangement = Arrangement.spacedBy(4.dp, Alignment.Bottom),
                    reverseLayout = false
                ) {
                    items(logEntries) { logEntry ->
                        LogEntryItem(logEntry = logEntry)
                    }
                }
            }
        }
    }
}

@Composable
private fun EmptyLogsState() {
    Column(
        modifier = Modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = if (LogManager.isLogEnabled()) {
                stringResource(id = R.string.no_logs_yet)
            } else {
                stringResource(id = R.string.logging_disabled)
            },
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center
        )
    }
}

@Composable
private fun LogEntryItem(logEntry: LogEntry) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(8.dp),
        colors = CardDefaults.cardColors(),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp)
        ) {
            // Header with timestamp, level, and tag
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = logEntry.getFormattedTime(),
                    style = MaterialTheme.typography.bodySmall.copy(
                        fontFamily = FontFamily.Monospace
                    ),
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.weight(1f)
                )
                
                Spacer(modifier = Modifier.width(8.dp))
                
                // Log level badge
                LogLevelBadge(level = logEntry.level)
                
                Spacer(modifier = Modifier.width(8.dp))
                
                Text(
                    text = logEntry.tag,
                    style = MaterialTheme.typography.bodySmall.copy(
                        fontFamily = FontFamily.Monospace,
                        fontWeight = FontWeight.Normal
                    ),
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            
            Spacer(modifier = Modifier.height(4.dp))
            
            // Message
            Text(
                text = logEntry.message,
                style = MaterialTheme.typography.bodyMedium.copy(
                    fontFamily = FontFamily.Monospace
                ),
                color = MaterialTheme.colorScheme.onSurface
            )
            
            // Stack trace if available
            logEntry.throwable?.let { throwable ->
                Spacer(modifier = Modifier.height(8.dp))
                
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(
                            color = MaterialTheme.colorScheme.errorContainer,
                            shape = RoundedCornerShape(4.dp)
                        )
                        .padding(8.dp)
                ) {
                    Text(
                        text = android.util.Log.getStackTraceString(throwable),
                        style = MaterialTheme.typography.bodySmall.copy(
                            fontFamily = FontFamily.Monospace
                        ),
                        color = MaterialTheme.colorScheme.onErrorContainer
                    )
                }
            }
        }
    }
}

@Composable
private fun LogLevelBadge(level: LogEntry.LogLevel) {
    val (color, fontWeight) = when (level) {
        LogEntry.LogLevel.VERBOSE -> LogColors.Verbose to FontWeight.Normal
        LogEntry.LogLevel.DEBUG -> LogColors.Debug to FontWeight.Normal
        LogEntry.LogLevel.INFO -> LogColors.Info to FontWeight.Bold
        LogEntry.LogLevel.WARN -> LogColors.Warn to FontWeight.Bold
        LogEntry.LogLevel.ERROR -> LogColors.Error to FontWeight.Bold
        LogEntry.LogLevel.ASSERT -> LogColors.Assert to FontWeight.Bold
    }
    
    Box(
        modifier = Modifier
            .background(
                color = MaterialTheme.colorScheme.surfaceVariant,
                shape = RoundedCornerShape(4.dp)
            )
            .padding(horizontal = 6.dp, vertical = 2.dp)
    ) {
        Text(
            text = level.shortName,
            style = MaterialTheme.typography.bodySmall.copy(
                fontFamily = FontFamily.Monospace,
                fontWeight = fontWeight
            ),
            color = color
        )
    }
}

private fun copyLogsToClipboard(context: Context, logEntries: List<LogEntry>) {
    if (logEntries.isEmpty()) {
        Toast.makeText(
            context,
            context.getString(R.string.no_logs_to_copy),
            Toast.LENGTH_SHORT
        ).show()
        return
    }
    
    val logText = logEntries.joinToString("\n") { it.getFormattedMessage() }
    
    val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
    val clip = ClipData.newPlainText(context.getString(R.string.app_logs), logText)
    clipboard.setPrimaryClip(clip)
    
    Toast.makeText(
        context,
        context.getString(R.string.logs_copied_to_clipboard, logEntries.size),
        Toast.LENGTH_SHORT
    ).show()
}

package now.link.ui.screens

import android.Manifest
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.PowerManager
import android.provider.Settings
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.ScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.google.accompanist.permissions.*
import now.link.R
import now.link.model.AgentConfiguration
import now.link.service.NezhaAgentService
import now.link.ui.components.ConfigurationDialog
import now.link.ui.components.WakeLockInfoDialog
import now.link.utils.Constants
import now.link.utils.LogManager
import now.link.utils.SPUtils
import now.link.utils.ThemeManager
import now.link.viewmodel.MainViewModel

private const val TAG = "MainScreen"

@OptIn(ExperimentalPermissionsApi::class)
@Composable
fun MainScreen(
    viewModel: MainViewModel = viewModel(),
    scrollState: ScrollState = rememberScrollState(),
    onLogsClick: () -> Unit = {},
) {
    val context = LocalContext.current

    // Observe ViewModel state
    val isServiceRunning by viewModel.isServiceRunning.observeAsState(false)
    val isRootAvailable by viewModel.isRootAvailable.observeAsState(false)
    val deviceArchitecture by viewModel.deviceArchitecture.observeAsState("")
    val agentConfiguration by viewModel.agentConfiguration.observeAsState()

    // Local state
    var isWakeLockEnabled by remember { mutableStateOf(SPUtils.getBoolean(Constants.Preferences.WAKE_LOCK_ENABLED)) }
    var isLoggingEnabled by remember { mutableStateOf(LogManager.isLogEnabled()) }
    var buttonText by remember { mutableStateOf("") }
    var showPermissionHintDialog by remember { mutableStateOf(false) }
    var showConfigurationDialog by remember { mutableStateOf(false) }
    var showWakeLockDialog by remember { mutableStateOf(false) }

    // Permissions setup using Accompanist
    val requiredPermissions = buildList {
        // Note: INTERNET is a normal permission, granted at install time
        // Only include dangerous permissions that require runtime permission
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            add(Manifest.permission.POST_NOTIFICATIONS)
        }
        // Add other dangerous permissions if needed
    }

    val permissionsState = rememberMultiplePermissionsState(requiredPermissions)

    // Battery optimization launcher
    val batteryOptimizationLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        LogManager.d(TAG, "Battery optimization result: ${result.resultCode}")
    }

    // Initialize logging when the screen first loads
    LaunchedEffect(Unit) {
        viewModel.initializeAgent(context)
    }

    // Update button state based on service status
    LaunchedEffect(isServiceRunning) {
        buttonText = if (isServiceRunning) {
            context.getString(R.string.stop_agent)
        } else {
            context.getString(R.string.start_agent)
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(scrollState)
            .windowInsetsPadding(WindowInsets.systemBars)
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(24.dp)
    ) {
        // App Header with Hero Section
        AppHeaderCard()

        // Status Card
        StatusCard(
            isServiceRunning = isServiceRunning,
            isRootAvailable = isRootAvailable,
            deviceArchitecture = deviceArchitecture,
            serverConfiguration = agentConfiguration?.server ?: ""
        )

        // Service Control Card
        ServiceControlCard(
            isServiceRunning = isServiceRunning,
            buttonText = buttonText,
            onServiceControlClick = {
                // Handle service control
                if (isServiceRunning) {
                    // Stop service
                    val stopIntent = Intent(context, NezhaAgentService::class.java).apply {
                        action = Constants.Service.ACTION_STOP
                    }
                    context.stopService(stopIntent)
                } else {
                    // Start service (check configuration first)
                    agentConfiguration?.let { config ->
                        if (config.server.isNotEmpty() && config.secret.isNotEmpty()) {
                            if (isBatteryOptimizationExempted(context)) {
                                LogManager.d(TAG, "Start Nezha Agent Service")
                                startNezhaAgentService(context, isWakeLockEnabled)
                            } else {
                                LogManager.d(TAG, "Show permission hint dialog")
                                showPermissionHintDialog = true
                            }
                        } else {
                            showConfigurationDialog = true
                        }
                    } ?: {
                        showConfigurationDialog = true
                    }
                }
            },
            onConfigureClick = { showConfigurationDialog = true }
        )

        // Advanced Settings Card
        AdvancedSettingsCard(
            isWakeLockEnabled = isWakeLockEnabled,
            isLoggingEnabled = isLoggingEnabled,
            onWakeLockChanged = { enabled ->
                isWakeLockEnabled = enabled
                // Save to preferences
                SPUtils.setBoolean(Constants.Preferences.WAKE_LOCK_ENABLED, enabled)
                LogManager.d(TAG, "Wake lock preference saved: $enabled")

                if (enabled) {
                    showWakeLockDialog = true
                }
            },
            onLoggingChanged = { enabled ->
                isLoggingEnabled = enabled
                LogManager.setLogEnabled(enabled)
                LogManager.i(TAG, "Logging preference changed: $enabled")
            }
        )

        // Theme Settings Card
        ThemeSettingsCard(
            isDynamicColorEnabled = ThemeManager.isDynamicColorEnabled,
            isFollowSystemTheme = ThemeManager.isFollowSystemTheme,
            isDarkModeEnabled = ThemeManager.isDarkModeEnabled,
            onDynamicColorChanged = { enabled ->
                ThemeManager.setDynamicColorEnabled(enabled)
                LogManager.d(TAG, "Dynamic color preference saved: $enabled")
            },
            onFollowSystemThemeChanged = { enabled ->
                ThemeManager.setFollowSystemTheme(enabled)
                LogManager.d(TAG, "Follow system theme preference saved: $enabled")
            },
            onDarkModeChanged = { enabled ->
                ThemeManager.setDarkModeEnabled(enabled)
                LogManager.d(TAG, "Dark mode preference saved: $enabled")
            }
        )

        // Actions Card
        ActionsCard(onLogsClick = onLogsClick)
    }

    // Permission Dialog
    if (showPermissionHintDialog) {
        PermissionDialog(
            permissionsState = permissionsState,
            onOpenSettings = {
                showPermissionHintDialog = false
                val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
                    data = Uri.parse("${Constants.Intent.PACKAGE_URI_PREFIX}${context.packageName}")
                }
                context.startActivity(intent)
            },
            onContinueAnyway = {
                showPermissionHintDialog = false
                LogManager.d(TAG, "User chose to continue without all permissions")
                requestBatteryOptimizationExemption(context, batteryOptimizationLauncher)
            },
            onDismiss = { showPermissionHintDialog = false },
            onRetryPermissions = {
                showPermissionHintDialog = false
                permissionsState.launchMultiplePermissionRequest()
            }
        )
    }

    // Configuration Dialog
    if (showConfigurationDialog) {
        val currentConfig =
            viewModel.agentConfiguration.value ?: AgentConfiguration("", "", "", "", true)

        ConfigurationDialog(
            configuration = currentConfig,
            onDismiss = { showConfigurationDialog = false },
            onSave = { config ->
                viewModel.updateConfiguration(config.server, config.secret, config.uuid)
                showConfigurationDialog = false
                Toast.makeText(
                    context,
                    context.getString(R.string.configuration_saved),
                    Toast.LENGTH_SHORT
                ).show()
            }
        )
    }

    // Wake Lock Dialog
    if (showWakeLockDialog) {
        WakeLockInfoDialog(
            onDismiss = { showWakeLockDialog = false }
        )
    }
}

@Composable
private fun AppHeaderCard() {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors()
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // App icon placeholder
            Icon(
                painter = painterResource(id = R.drawable.ic_stat_name),
                contentDescription = null,
                modifier = Modifier.size(80.dp),
            )

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = stringResource(id = R.string.app_name),
                style = MaterialTheme.typography.headlineMedium,
                color = MaterialTheme.colorScheme.onSurface,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = stringResource(id = R.string.app_description),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center
            )
        }
    }
}

@Composable
private fun StatusCard(
    isServiceRunning: Boolean,
    isRootAvailable: Boolean,
    deviceArchitecture: String,
    serverConfiguration: String,
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.outlinedCardColors(),
        border = CardDefaults.outlinedCardBorder()
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp)
        ) {
            Text(
                text = stringResource(id = R.string.system_information),
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurface
            )

            Spacer(modifier = Modifier.height(16.dp))

            StatusRow(
                label = stringResource(id = R.string.agent_running),
                value = if (isServiceRunning) {
                    stringResource(id = R.string.agent_running)
                } else {
                    stringResource(id = R.string.agent_stopped)
                },
                isPositive = isServiceRunning
            )

            StatusRow(
                label = stringResource(id = R.string.root_access),
                value = if (isRootAvailable) {
                    stringResource(id = R.string.root_available)
                } else {
                    stringResource(id = R.string.root_not_available)
                },
                isPositive = isRootAvailable
            )

            StatusRow(
                label = stringResource(id = R.string.architecture),
                value = deviceArchitecture.ifEmpty { stringResource(id = R.string.checking) }
            )

            StatusRow(
                label = stringResource(id = R.string.configuration),
                value = if (serverConfiguration.isNotEmpty()) {
                    stringResource(id = R.string.server_format, serverConfiguration)
                } else {
                    stringResource(id = R.string.not_configured)
                },
                isPositive = serverConfiguration.isNotEmpty()
            )
        }
    }
}

@Composable
private fun StatusRow(
    label: String,
    value: String,
    isPositive: Boolean? = null,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.weight(1f)
        )

        Text(
            text = value,
            style = MaterialTheme.typography.bodyMedium,
            color = when (isPositive) {
                true -> MaterialTheme.colorScheme.primary
                false -> MaterialTheme.colorScheme.error
                null -> MaterialTheme.colorScheme.onSurface
            },
            fontWeight = FontWeight.Medium
        )
    }
}

@Composable
private fun ServiceControlCard(
    isServiceRunning: Boolean,
    buttonText: String,
    onServiceControlClick: () -> Unit,
    onConfigureClick: () -> Unit,
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.outlinedCardColors(),
        border = CardDefaults.outlinedCardBorder()
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp)
        ) {
            Text(
                text = stringResource(id = R.string.nezha_agent_title),
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurface
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Service control button
            Button(
                onClick = onServiceControlClick,
                modifier = Modifier.fillMaxWidth(),
                enabled = true,
                colors = if (isServiceRunning) {
                    ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.error
                    )
                } else {
                    ButtonDefaults.buttonColors()
                }
            ) {
                Icon(
                    painter = painterResource(if (isServiceRunning) R.drawable.ic_stop else R.drawable.ic_play_arrow),
                    contentDescription = null,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(buttonText)
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Configure button
            OutlinedButton(
                onClick = onConfigureClick,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(stringResource(id = R.string.configure_server))
            }
        }
    }
}

@Composable
private fun AdvancedSettingsCard(
    isWakeLockEnabled: Boolean,
    isLoggingEnabled: Boolean,
    onWakeLockChanged: (Boolean) -> Unit,
    onLoggingChanged: (Boolean) -> Unit,
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.outlinedCardColors(),
        border = CardDefaults.outlinedCardBorder()
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp)
        ) {
            Text(
                text = stringResource(id = R.string.advanced_settings),
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurface
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Wake Lock Setting
            SettingRow(
                title = stringResource(id = R.string.acquire_wake_lock),
                description = stringResource(id = R.string.wake_lock_description),
                isEnabled = isWakeLockEnabled,
                onToggle = onWakeLockChanged
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Logging Setting
            SettingRow(
                title = stringResource(id = R.string.enable_logging),
                description = stringResource(id = R.string.logging_description),
                isEnabled = isLoggingEnabled,
                onToggle = onLoggingChanged
            )
        }
    }
}

@Composable
private fun SettingRow(
    title: String,
    description: String,
    isEnabled: Boolean,
    onToggle: (Boolean) -> Unit,
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(
            modifier = Modifier.weight(1f)
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurface
            )

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = description,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        Spacer(modifier = Modifier.width(16.dp))

        Switch(
            checked = isEnabled,
            onCheckedChange = onToggle
        )
    }
}

@Composable
private fun ThemeSettingsCard(
    isDynamicColorEnabled: Boolean,
    isFollowSystemTheme: Boolean,
    isDarkModeEnabled: Boolean,
    onDynamicColorChanged: (Boolean) -> Unit,
    onFollowSystemThemeChanged: (Boolean) -> Unit,
    onDarkModeChanged: (Boolean) -> Unit,
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.outlinedCardColors(),
        border = CardDefaults.outlinedCardBorder()
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp)
        ) {
            Text(
                text = stringResource(id = R.string.theme_settings),
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurface
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Dynamic Color Setting (only show if supported)
            if (ThemeManager.isDynamicColorSupported()) {
                SettingRow(
                    title = stringResource(id = R.string.material_you_dynamic_color),
                    description = stringResource(id = R.string.material_you_description),
                    isEnabled = isDynamicColorEnabled,
                    onToggle = onDynamicColorChanged
                )

                Spacer(modifier = Modifier.height(16.dp))
            }

            // Follow System Theme Setting
            SettingRow(
                title = stringResource(id = R.string.follow_system_theme),
                description = stringResource(id = R.string.follow_system_description),
                isEnabled = isFollowSystemTheme,
                onToggle = onFollowSystemThemeChanged
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Dark Mode Setting (disabled when following system theme)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(
                    modifier = Modifier.weight(1f)
                ) {
                    Text(
                        text = stringResource(id = R.string.dark_mode),
                        style = MaterialTheme.typography.bodyMedium,
                        color = if (isFollowSystemTheme) {
                            MaterialTheme.colorScheme.onSurfaceVariant
                        } else {
                            MaterialTheme.colorScheme.onSurface
                        }
                    )

                    Spacer(modifier = Modifier.height(4.dp))

                    Text(
                        text = stringResource(id = R.string.dark_mode_description),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                Spacer(modifier = Modifier.width(16.dp))

                Switch(
                    checked = isDarkModeEnabled,
                    onCheckedChange = onDarkModeChanged,
                    enabled = !isFollowSystemTheme
                )
            }
        }
    }
}

@Composable
private fun ActionsCard(
    onLogsClick: () -> Unit,
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.outlinedCardColors(),
        border = CardDefaults.outlinedCardBorder()
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp)
        ) {
            Text(
                text = stringResource(id = R.string.actions),
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurface
            )

            Spacer(modifier = Modifier.height(16.dp))

            OutlinedButton(
                onClick = onLogsClick,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(stringResource(id = R.string.view_logs))
            }
        }
    }
}

@OptIn(ExperimentalPermissionsApi::class)
@Composable
private fun PermissionDialog(
    permissionsState: MultiplePermissionsState,
    onOpenSettings: () -> Unit,
    onContinueAnyway: () -> Unit,
    onRetryPermissions: () -> Unit,
    onDismiss: () -> Unit,
) {
    val context = LocalContext.current

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(stringResource(id = R.string.permissions_required))
        },
        text = {
            Column {
                Text(stringResource(id = R.string.permissions_message))

                Spacer(modifier = Modifier.height(12.dp))

                // Show which permissions are denied
                permissionsState.permissions.forEach { permission ->
                    if (!permission.status.isGranted) {
                        val permissionName = when (permission.permission) {
                            Manifest.permission.INTERNET -> stringResource(id = R.string.internet_access)
                            Manifest.permission.POST_NOTIFICATIONS -> stringResource(id = R.string.notifications)
                            else -> stringResource(id = R.string.unknown_permission)
                        }
                        Text(
                            text = stringResource(id = R.string.permission_item, permissionName),
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.error
                        )
                    }
                }
            }
        },
        confirmButton = {
            if (permissionsState.shouldShowRationale) {
                TextButton(onClick = onRetryPermissions) {
                    Text(stringResource(id = R.string.retry))
                }
            } else {
                TextButton(onClick = onOpenSettings) {
                    Text(stringResource(id = R.string.settings))
                }
            }
        },
        dismissButton = {
            TextButton(onClick = onContinueAnyway) {
                Text(stringResource(id = R.string.continue_anyway))
            }
        }
    )
}

/**
 * Start the Nezha Agent Service with the given context.
 */
private fun startNezhaAgentService(context: Context, isWakeLockEnabled: Boolean) {
    val startIntent =
        Intent(context, NezhaAgentService::class.java).apply {
            putExtra(
                Constants.Service.EXTRA_WAKE_LOCK_ENABLED,
                isWakeLockEnabled
            )
        }
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
        context.startForegroundService(startIntent)
    } else {
        context.startService(startIntent)
    }
}

/**
 * Check if the app is exempted from battery optimization.
 */
private fun isBatteryOptimizationExempted(context: Context): Boolean {
    val powerManager = context.getSystemService(Context.POWER_SERVICE) as PowerManager

    return powerManager.isIgnoringBatteryOptimizations(context.packageName)
}

/**
 * Request battery optimization exemption for the app.
 */
private fun requestBatteryOptimizationExemption(
    context: Context,
    batteryOptimizationLauncher: androidx.activity.result.ActivityResultLauncher<Intent>,
) {
    LogManager.d(TAG, "Requesting battery optimization exemption...")

    val powerManager = context.getSystemService(Context.POWER_SERVICE) as PowerManager

    // Check if already exempted
    if (powerManager.isIgnoringBatteryOptimizations(context.packageName)) {
        LogManager.d(TAG, "Already exempted from battery optimization")
        return
    }

    LogManager.d(TAG, "Launching battery optimization settings")
    val intent = Intent(Settings.ACTION_REQUEST_IGNORE_BATTERY_OPTIMIZATIONS).apply {
        data = Uri.parse("${Constants.Intent.PACKAGE_URI_PREFIX}${context.packageName}")
    }
    try {
        batteryOptimizationLauncher.launch(intent)
    } catch (e: Exception) {
        LogManager.w(TAG, "Failed to launch battery optimization settings, continuing anyway", e)
    }
}

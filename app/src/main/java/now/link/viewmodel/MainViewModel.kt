package now.link.viewmodel

import android.annotation.SuppressLint
import android.app.Application
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.PowerManager
import android.provider.Settings
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.asLiveData
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import now.link.agent.AgentConfiguration
import now.link.agent.AgentType
import now.link.agent.UnifiedConfigurationManager
import now.link.service.UnifiedAgentService
import now.link.utils.UnifiedAgentManager
import now.link.utils.Constants
import now.link.utils.LogManager
import now.link.utils.RootUtils
import now.link.utils.ServiceStatusManager
import now.link.utils.SPUtils
import androidx.core.net.toUri

private const val TAG = "MainViewModel"

// Sealed classes for specific state types
sealed class ServiceAction {
    object Idle : ServiceAction()
    object Starting : ServiceAction()
    object Stopping : ServiceAction()
}

sealed class PermissionState {
    object Unknown : PermissionState()
    object Granted : PermissionState()
    object Denied : PermissionState()
    object ShowDialog : PermissionState()
}

sealed class BatteryOptimizationState {
    object Unknown : BatteryOptimizationState()
    object Exempted : BatteryOptimizationState()
    object NotExempted : BatteryOptimizationState()
    object ShowDialog : BatteryOptimizationState()
}

// Data class for the overall UI state
data class MainScreenUiState(
    val isServiceRunning: Boolean = false,
    val isRootAvailable: Boolean = false,
    val deviceArchitecture: String = "",
    val agentConfiguration: AgentConfiguration? = null,
    val currentAgentType: AgentType = AgentType.NEZHA_AGENT,
    val availableAgentTypes: List<AgentType> = emptyList(),
    val installedAgentTypes: List<AgentType> = emptyList(),
    val isWakeLockEnabled: Boolean = false,
    val isLoggingEnabled: Boolean = false,

    // Action states using sealed classes
    val serviceAction: ServiceAction = ServiceAction.Idle,
    val permissionState: PermissionState = PermissionState.Unknown,
    val batteryOptimizationState: BatteryOptimizationState = BatteryOptimizationState.Unknown,

    // Dialog states
    val showConfigurationDialog: Boolean = false,
    val showWakeLockDialog: Boolean = false,

    // Error handling
    val errorMessage: String? = null,
    val toastMessage: String? = null,
)

class MainViewModel(application: Application) : AndroidViewModel(application) {

    private val configManager = UnifiedConfigurationManager(application)
    private val agentManager = UnifiedAgentManager(application)

    // Modern StateFlow approach instead of LiveData
    private val _uiState = MutableStateFlow(MainScreenUiState())
    val uiState: StateFlow<MainScreenUiState> = _uiState.asStateFlow()

    // Keep LiveData for service status since it comes from ServiceStatusManager
    val isServiceRunning: LiveData<Boolean> = ServiceStatusManager
        .observeServiceStatus(application)
        .asLiveData(viewModelScope.coroutineContext)

    init {
        loadInitialState()
        observeServiceStatus()
    }

    private fun loadInitialState() {
        viewModelScope.launch {
            _uiState.update { state ->
                state.copy(
                    agentConfiguration = configManager.loadConfiguration(),
                    currentAgentType = configManager.getCurrentAgentType(),
                    availableAgentTypes = agentManager.getAvailableAgentTypes(),
                    installedAgentTypes = agentManager.findInstalledAgents(),
                    isWakeLockEnabled = SPUtils.getBoolean(Constants.Preferences.WAKE_LOCK_ENABLED),
                    isLoggingEnabled = LogManager.isLogEnabled(),
                    deviceArchitecture = agentManager.getDeviceArchitecture(),
                    isRootAvailable = RootUtils.isRootAvailable()
                )
            }
        }
    }

    private fun observeServiceStatus() {
        // Update UI state when service status changes
        isServiceRunning.observeForever { isRunning ->
            _uiState.update { it.copy(isServiceRunning = isRunning) }
        }
    }

    // Service control methods
    fun startService(context: Context) {
        viewModelScope.launch {
            _uiState.update { it.copy(serviceAction = ServiceAction.Starting) }

            try {
                // Check configuration first
                val config = _uiState.value.agentConfiguration
                if (config?.server.isNullOrEmpty() || config.secret.isEmpty()) {
                    _uiState.update { 
                        it.copy(
                            serviceAction = ServiceAction.Idle,
                            showConfigurationDialog = true
                        ) 
                    }
                    return@launch
                }

                // Check permissions
                if (!checkPermissions(context)) {
                    _uiState.update { 
                        it.copy(
                            serviceAction = ServiceAction.Idle,
                            permissionState = PermissionState.ShowDialog
                        ) 
                    }
                    return@launch
                }

                // Check battery optimization
                if (!isBatteryOptimizationExempted(context)) {
                    _uiState.update { 
                        it.copy(
                            serviceAction = ServiceAction.Idle,
                            batteryOptimizationState = BatteryOptimizationState.ShowDialog
                        ) 
                    }
                    return@launch
                }

                // All checks passed, start service
                startUnifiedAgentService(context)
            } catch (e: Exception) {
                LogManager.e(TAG, "Failed to start service", e)
                _uiState.update { 
                    it.copy(
                        serviceAction = ServiceAction.Idle,
                        errorMessage = "Failed to start service: ${e.message}"
                    ) 
                }
            }
        }
    }

    fun stopService(context: Context) {
        viewModelScope.launch {
            _uiState.update { it.copy(serviceAction = ServiceAction.Stopping) }
            
            try {
                val stopIntent = Intent(context, UnifiedAgentService::class.java).apply {
                    action = Constants.Service.ACTION_STOP
                }
                context.stopService(stopIntent)
                
                _uiState.update { it.copy(serviceAction = ServiceAction.Idle) }
            } catch (e: Exception) {
                LogManager.e(TAG, "Failed to stop service", e)
                _uiState.update { 
                    it.copy(
                        serviceAction = ServiceAction.Idle,
                        errorMessage = "Failed to stop service: ${e.message}"
                    ) 
                }
            }
        }
    }

    private fun startUnifiedAgentService(context: Context) {
        val startIntent = Intent(context, UnifiedAgentService::class.java).apply {
            putExtra(Constants.Service.EXTRA_WAKE_LOCK_ENABLED, _uiState.value.isWakeLockEnabled)
        }
        
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            context.startForegroundService(startIntent)
        } else {
            context.startService(startIntent)
        }
        
        _uiState.update { it.copy(serviceAction = ServiceAction.Idle) }
        LogManager.d(TAG, "Unified Agent Service started")
    }

    private fun checkPermissions(context: Context): Boolean {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            // Check POST_NOTIFICATIONS permission
            return context.checkSelfPermission(android.Manifest.permission.POST_NOTIFICATIONS) == 
                android.content.pm.PackageManager.PERMISSION_GRANTED
        }
        return true // No dangerous permissions required for older versions
    }

    private fun isBatteryOptimizationExempted(context: Context): Boolean {
        val powerManager = context.getSystemService(Context.POWER_SERVICE) as PowerManager
        return powerManager.isIgnoringBatteryOptimizations(context.packageName)
    }

    // Permission handling
    fun onPermissionsGranted(context: Context) {
        _uiState.update { it.copy(permissionState = PermissionState.Granted) }
        // Continue with service start flow
        startService(context)
    }

    fun onPermissionsDenied(context: Context) {
        _uiState.update { it.copy(permissionState = PermissionState.Denied) }
        // Continue anyway but check battery optimization
        if (!isBatteryOptimizationExempted(context)) {
            _uiState.update { it.copy(batteryOptimizationState = BatteryOptimizationState.ShowDialog) }
        } else {
            startUnifiedAgentService(context)
        }
    }

    fun dismissPermissionDialog() {
        _uiState.update { it.copy(permissionState = PermissionState.Unknown) }
    }

    // Battery optimization handling
    fun onBatteryOptimizationExempted(context: Context) {
        _uiState.update { it.copy(batteryOptimizationState = BatteryOptimizationState.Exempted) }
        startUnifiedAgentService(context)
    }

    fun onBatteryOptimizationDenied(context: Context) {
        _uiState.update { 
            it.copy(
                batteryOptimizationState = BatteryOptimizationState.NotExempted,
                toastMessage = "Battery optimization not disabled. The service may be killed by the system to save power."
            ) 
        }
        startUnifiedAgentService(context)
    }

    fun dismissBatteryOptimizationDialog() {
        _uiState.update { it.copy(batteryOptimizationState = BatteryOptimizationState.Unknown) }
    }

    @SuppressLint("BatteryLife")
    fun requestBatteryOptimizationExemption(context: Context): Intent? {
        val powerManager = context.getSystemService(Context.POWER_SERVICE) as PowerManager
        
        return if (!powerManager.isIgnoringBatteryOptimizations(context.packageName)) {
            Intent(Settings.ACTION_REQUEST_IGNORE_BATTERY_OPTIMIZATIONS).apply {
                data = "${Constants.Intent.PACKAGE_URI_PREFIX}${context.packageName}".toUri()
            }
        } else {
            LogManager.d(TAG, "Already exempted from battery optimization")
            null
        }
    }

    // Configuration handling
    fun showConfigurationDialog() {
        _uiState.update { it.copy(showConfigurationDialog = true) }
    }

    fun dismissConfigurationDialog() {
        _uiState.update { it.copy(showConfigurationDialog = false) }
    }

    fun updateConfiguration(agentType: AgentType, config: AgentConfiguration) {
        // Update agent type if changed
        if (agentType != configManager.getCurrentAgentType()) {
            configManager.setCurrentAgentType(agentType)
        }
        
        configManager.saveConfiguration(config)
        _uiState.update { 
            it.copy(
                agentConfiguration = config,
                currentAgentType = agentType,
                showConfigurationDialog = false,
                toastMessage = "Configuration saved"
            ) 
        }
    }

    fun changeAgentType(agentType: AgentType) {
        configManager.setCurrentAgentType(agentType)
        _uiState.update { 
            it.copy(
                currentAgentType = agentType,
                agentConfiguration = configManager.loadConfiguration()
            ) 
        }
    }

    // Settings handling
    fun updateWakeLockEnabled(enabled: Boolean) {
        SPUtils.setBoolean(Constants.Preferences.WAKE_LOCK_ENABLED, enabled)
        _uiState.update { 
            it.copy(
                isWakeLockEnabled = enabled,
                showWakeLockDialog = if (enabled) true else it.showWakeLockDialog
            ) 
        }
        LogManager.d(TAG, "Wake lock preference saved: $enabled")
    }

    fun updateLoggingEnabled(enabled: Boolean) {
        LogManager.setLogEnabled(enabled)
        _uiState.update { it.copy(isLoggingEnabled = enabled) }
        LogManager.i(TAG, "Logging preference changed: $enabled")
    }

    fun showWakeLockDialog() {
        _uiState.update { it.copy(showWakeLockDialog = true) }
    }

    fun dismissWakeLockDialog() {
        _uiState.update { it.copy(showWakeLockDialog = false) }
    }

    // Error and toast handling
    fun clearError() {
        _uiState.update { it.copy(errorMessage = null) }
    }

    fun clearToast() {
        _uiState.update { it.copy(toastMessage = null) }
    }
}

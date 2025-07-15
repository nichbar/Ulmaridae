package now.link.viewmodel

import android.app.Application
import android.content.Context
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch
import now.link.model.AgentConfiguration
import now.link.utils.AgentManager
import now.link.utils.ConfigurationManager
import now.link.utils.RootUtils

class MainViewModel(application: Application) : AndroidViewModel(application) {
    
    private val configManager = ConfigurationManager(application)
    private val agentManager = AgentManager(application)
    
    private val _isServiceRunning = MutableLiveData<Boolean>()
    val isServiceRunning: LiveData<Boolean> = _isServiceRunning
    
    private val _isRootAvailable = MutableLiveData<Boolean>()
    val isRootAvailable: LiveData<Boolean> = _isRootAvailable
    
    private val _agentConfiguration = MutableLiveData<AgentConfiguration>()
    val agentConfiguration: LiveData<AgentConfiguration> = _agentConfiguration
    
    private val _isAgentInstalled = MutableLiveData<Boolean>()
    val isAgentInstalled: LiveData<Boolean> = _isAgentInstalled
    
    init {
        loadConfiguration()
        checkAgentInstallation()
        checkRootAccess()
    }
    
    fun initializeAgent(context: Context) {
        viewModelScope.launch {
            if (!agentManager.isAgentInstalled()) {
                val success = agentManager.downloadAndInstallAgent()
                _isAgentInstalled.value = success
            } else {
                _isAgentInstalled.value = true
            }
        }
    }
    
    private fun loadConfiguration() {
        _agentConfiguration.value = configManager.loadConfiguration()
    }
    
    private fun checkAgentInstallation() {
        _isAgentInstalled.value = agentManager.isAgentInstalled()
    }
    
    private fun checkRootAccess() {
        viewModelScope.launch {
            _isRootAvailable.value = RootUtils.isRootAvailable()
        }
    }
    
    fun updateConfiguration(server: String, secret: String) {
        val config = AgentConfiguration(
            server = server,
            secret = secret,
            clientId = _agentConfiguration.value?.clientId ?: "",
            enableTLS = _agentConfiguration.value?.enableTLS ?: true
        )
        
        configManager.saveConfiguration(config)
        _agentConfiguration.value = config
    }
    
    fun setServiceRunning(isRunning: Boolean) {
        _isServiceRunning.value = isRunning
    }
}

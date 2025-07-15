package now.link

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.lifecycle.ViewModelProvider
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import now.link.databinding.ActivityMainBinding
import now.link.service.NezhaAgentService
import now.link.viewmodel.MainViewModel

class MainActivity : AppCompatActivity() {
    
    private lateinit var binding: ActivityMainBinding
    private lateinit var viewModel: MainViewModel
    
    private val requestPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        val allGranted = permissions.values.all { it }
        if (allGranted) {
            initializeAgent()
        } else {
            showPermissionDeniedDialog()
        }
    }
    
    private val batteryOptimizationLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { 
        // Battery optimization permission handled
        checkAndRequestPermissions()
    }
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        
        viewModel = ViewModelProvider(this)[MainViewModel::class.java]
        
        setupUI()
        observeViewModel()
        checkAndRequestPermissions()
    }
    
    private fun setupUI() {
        binding.apply {
            switchService.setOnCheckedChangeListener { _, isChecked ->
                if (isChecked) {
                    if (isConfigurationValid()) {
                        startNezhaService()
                    } else {
                        switchService.isChecked = false
                        showConfigurationDialog()
                    }
                } else {
                    stopNezhaService()
                }
            }
            
            buttonConfigure.setOnClickListener {
                showConfigurationDialog()
            }
            
            buttonLogs.setOnClickListener {
                // TODO: Show logs activity
                Toast.makeText(this@MainActivity, "Logs feature coming soon", Toast.LENGTH_SHORT).show()
            }
        }
    }
    
    private fun observeViewModel() {
        viewModel.isServiceRunning.observe(this) { isRunning ->
            binding.switchService.isChecked = isRunning
            binding.textStatus.text = if (isRunning) {
                "Nezha Agent is running"
            } else {
                "Nezha Agent is stopped"
            }
        }
        
        viewModel.isRootAvailable.observe(this) { hasRoot ->
            binding.textRootStatus.text = if (hasRoot) {
                "Root access: Available"
            } else {
                "Root access: Not available"
            }
        }
        
        viewModel.agentConfiguration.observe(this) { config ->
            binding.textConfig.text = if (config.server.isNotEmpty()) {
                "Server: ${config.server}"
            } else {
                "Not configured"
            }
        }
    }
    
    private fun checkAndRequestPermissions() {
        val permissions = mutableListOf<String>()
        
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.INTERNET) 
            != PackageManager.PERMISSION_GRANTED) {
            permissions.add(Manifest.permission.INTERNET)
        }
        
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS) 
                != PackageManager.PERMISSION_GRANTED) {
                permissions.add(Manifest.permission.POST_NOTIFICATIONS)
            }
        }
        
        if (permissions.isNotEmpty()) {
            requestPermissionLauncher.launch(permissions.toTypedArray())
        } else {
            requestBatteryOptimizationExemption()
        }
    }
    
    private fun requestBatteryOptimizationExemption() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            val intent = Intent(Settings.ACTION_REQUEST_IGNORE_BATTERY_OPTIMIZATIONS).apply {
                data = Uri.parse("package:$packageName")
            }
            try {
                batteryOptimizationLauncher.launch(intent)
            } catch (e: Exception) {
                initializeAgent()
            }
        } else {
            initializeAgent()
        }
    }
    
    private fun initializeAgent() {
        viewModel.initializeAgent(this)
    }
    
    private fun isConfigurationValid(): Boolean {
        val config = viewModel.agentConfiguration.value
        return config?.server?.isNotEmpty() == true && config.secret.isNotEmpty()
    }
    
    private fun startNezhaService() {
        val intent = Intent(this, NezhaAgentService::class.java)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            startForegroundService(intent)
        } else {
            startService(intent)
        }
    }
    
    private fun stopNezhaService() {
        val intent = Intent(this, NezhaAgentService::class.java)
        stopService(intent)
    }
    
    private fun showConfigurationDialog() {
        val config = viewModel.agentConfiguration.value ?: return
        
        val dialogView = layoutInflater.inflate(R.layout.dialog_configuration, null)
        val editServer = dialogView.findViewById<com.google.android.material.textfield.TextInputEditText>(R.id.editServer)
        val editSecret = dialogView.findViewById<com.google.android.material.textfield.TextInputEditText>(R.id.editSecret)
        
        editServer.setText(config.server)
        editSecret.setText(config.secret)
        
        MaterialAlertDialogBuilder(this)
            .setTitle("Configure Nezha Agent")
            .setView(dialogView)
            .setPositiveButton("Save") { _, _ ->
                val server = editServer.text.toString().trim()
                val secret = editSecret.text.toString().trim()
                
                if (server.isNotEmpty() && secret.isNotEmpty()) {
                    viewModel.updateConfiguration(server, secret)
                    Toast.makeText(this, "Configuration saved", Toast.LENGTH_SHORT).show()
                } else {
                    Toast.makeText(this, "Please fill all fields", Toast.LENGTH_SHORT).show()
                }
            }
            .setNegativeButton("Cancel", null)
            .show()
    }
    
    private fun showPermissionDeniedDialog() {
        MaterialAlertDialogBuilder(this)
            .setTitle("Permissions Required")
            .setMessage("This app requires certain permissions to function properly. Please grant the permissions in the app settings.")
            .setPositiveButton("Settings") { _, _ ->
                val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
                    data = Uri.parse("package:$packageName")
                }
                startActivity(intent)
            }
            .setNegativeButton("Cancel", null)
            .show()
    }
}

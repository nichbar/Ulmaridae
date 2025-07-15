# Nezha Agent Android Wrapper

An Android wrapper application for [Nezha](https://github.com/nezhahq/agent) monitoring agent, designed to run the agent on Android devices even with root privileges.

## Features

- ✅ **Simple UI**: Clean Material Design interface with toggle controls
- ✅ **Background Service**: Runs Nezha agent as a persistent foreground service
- ✅ **Root Support**: Automatically detects and uses root privileges when available
- ✅ **Auto-start**: Automatically starts the agent on device boot
- ✅ **ARM Support**: Automatically downloads and bundles the appropriate ARM binary
- ✅ **Battery Optimization**: Requests exemption from battery optimization
- ✅ **Network Permissions**: Handles all required permissions automatically

## Installation

1. Build the APK:

   ```bash
   ./gradlew assembleDebug
   ```

2. Install on your Android device:
   ```bash
   adb install app/build/outputs/apk/debug/app-debug.apk
   ```

## Configuration

1. Open the app on your Android device
2. Tap "Configure" to set up your Nezha server details:

   - **Server URL**: Your Nezha server URL (e.g., `https://your-server.com:5555`)
   - **Agent Secret**: The secret key for your agent (found in Nezha dashboard)

3. Grant the necessary permissions when prompted:
   - Internet access
   - Network state access
   - Foreground service permission
   - Boot receiver permission
   - Battery optimization exemption

## Usage

### Starting the Agent

1. Open the app
2. Configure the server settings if not already done
3. Toggle the "Enable Nezha Agent" switch
4. The agent will start running in the background

### Background Operation

- The agent runs as a foreground service with a persistent notification
- It will automatically restart on device boot
- The service continues running even when the app is closed
- Root privileges are automatically used if available

### Stopping the Agent

1. Open the app
2. Toggle off the "Enable Nezha Agent" switch

Or use the notification:

1. Pull down the notification panel
2. Tap "Stop" on the Nezha Agent notification

## Architecture

### Components

- **MainActivity**: Main UI with configuration and control
- **NezhaAgentService**: Background service that manages the agent process
- **BootReceiver**: Handles auto-start on device boot
- **AgentManager**: Downloads and manages the Nezha agent binary
- **ConfigurationManager**: Handles settings storage
- **RootUtils**: Detects and uses root privileges

### Binary Management

The app automatically:

1. Detects device architecture (ARM64, ARM, x86_64, x86)
2. Downloads the appropriate Nezha agent binary from GitHub releases
3. Extracts and makes the binary executable
4. Stores it in the app's private files directory

### Root vs Non-Root Operation

**With Root Access:**

- Agent runs with elevated privileges
- Better system monitoring capabilities
- More accurate hardware information
- Can monitor system-level metrics

**Without Root Access:**

- Agent runs with standard app permissions
- Limited but functional monitoring
- Basic system information available
- Network and storage monitoring works

## Permissions Explained

| Permission                             | Purpose                                  |
| -------------------------------------- | ---------------------------------------- |
| `INTERNET`                             | Network communication with Nezha server  |
| `ACCESS_NETWORK_STATE`                 | Monitor network connectivity             |
| `FOREGROUND_SERVICE`                   | Run background service                   |
| `FOREGROUND_SERVICE_SYSTEM_EXEMPTED`   | System-level service exemption           |
| `WAKE_LOCK`                            | Keep device awake for monitoring         |
| `REQUEST_IGNORE_BATTERY_OPTIMIZATIONS` | Prevent Android from killing the service |
| `WRITE_EXTERNAL_STORAGE`               | Store agent binary and logs              |
| `RECEIVE_BOOT_COMPLETED`               | Auto-start on device boot                |
| `POST_NOTIFICATIONS`                   | Show service notifications (Android 13+) |
| `ACCESS_SUPERUSER`                     | Root access (if available)               |

## Troubleshooting

### Agent Won't Start

1. Check internet connectivity
2. Verify server URL and secret are correct
3. Check if agent binary downloaded successfully
4. Look at system logs: `adb logcat | grep NezhaAgentService`

### Service Keeps Stopping

1. Disable battery optimization for the app
2. Add the app to auto-start apps (varies by manufacturer)
3. Check if the device has aggressive memory management

### Root Detection Issues

1. Ensure your device is properly rooted
2. Grant root access to the app when prompted
3. Some root management apps may require manual approval

### Performance Impact

- The app has minimal impact on device performance
- Agent binary is lightweight (~6MB)
- Network usage depends on monitoring frequency
- Battery usage is optimized for background operation

## Building from Source

### Prerequisites

- Android Studio or Android SDK
- Java 11 or higher
- Gradle 8.11.0+

### Build Steps

```bash
# Clone the repository
git clone <your-repo-url>
cd nzwrapper

# Build debug APK
./gradlew assembleDebug

# Build release APK (requires signing config)
./gradlew assembleRelease
```

## Contributing

1. Fork the repository
2. Create a feature branch
3. Make your changes
4. Test on real Android devices
5. Submit a pull request

## License

This project is licensed under the Apache 2.0 License - see the [LICENSE](LICENSE) file for details.

## Acknowledgments

- [Nezha Project](https://github.com/nezhahq/agent) - The core monitoring agent
- Android Open Source Project - For the Android framework
- Material Design - For the UI components

## Support

For issues and questions:

1. Check the troubleshooting section above
2. Review device logs: `adb logcat | grep Nezha`
3. Open an issue on the repository

---

**Note**: This wrapper is designed for Android devices and has been tested on ARM-based Android smartphones and tablets. Root access is optional but recommended for full functionality.

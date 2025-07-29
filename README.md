# Ulmaridae

[![Build and Release](https://github.com/nichbar/Ulmaridae/actions/workflows/build-release.yml/badge.svg)](https://github.com/nichbar/Ulmaridae/actions/workflows/build-release.yml)
[![CI](https://github.com/nichbar/Ulmaridae/actions/workflows/ci.yml/badge.svg)](https://github.com/nichbar/Ulmaridae/actions/workflows/ci.yml)

Ulmaridae is an Android wrapper application for the [Nezha](https://github.com/nezhahq/nezha) monitoring agent, designed to run the agent on Android devices, even without root privileges.

The original [Nezha Agent](https://github.com/nezhahq/agent) is built for standard Linux environments, and some of its features will not work on Android, even if root privileges are granted. This project aims to make it work on Android with ease.

This project uses a modified version of the [Nezha Agent](https://github.com/nichbar/agent) to work better on Android.

All binaries are built from source using GitHub Actions.

## Installation & Configuration

1. Download and install the latest APK from the [releases page](https://github.com/nichbar/Ulmaridae/releases)
2. Open the app on your Android device
3. Tap "Configure" to set up your Nezha server details:

   - **Server URL**: Your Nezha server URL (e.g., `https://your-server.com:443`)
   - **Agent Secret**: The secret key for your agent (found in Nezha dashboard)

4. Grant the necessary permissions when prompted:
   - Foreground service permission
   - Battery optimization exemption
   - Root access (if available)

5. Toggle the "Enable Nezha Agent" switch to start the agent

## Usage

### Starting the Agent

1. Open the app
2. Configure the server settings if not already done
3. Toggle the "Enable Nezha Agent" switch
4. The agent will start running in the background

### Background Operation

- The agent runs as a foreground service with a persistent notification
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
- **AgentManager**: Extracts and manages the bundled Nezha agent binary
- **ConfigurationManager**: Handles settings storage
- **RootUtils**: Detects and uses root privileges

### Root vs Non-Root Operation

**With Root Access:**

- Agent runs with elevated privileges
- Better system monitoring capabilities
- More accurate hardware information
- Can monitor system-level metrics

**Without Root Access:**

- Agent runs with standard app permissions
- Limited but functional monitoring (app-level metrics)
- Basic system information available
- Network and storage monitoring works

## Permissions Explained

| Permission                             | Purpose                                  |
| -------------------------------------- | ---------------------------------------- |
| `INTERNET`                             | Network communication with server        |
| `ACCESS_NETWORK_STATE`                 | Monitor network connectivity             |
| `FOREGROUND_SERVICE`                   | Run background service                   |
| `FOREGROUND_SERVICE_SYSTEM_EXEMPTED`   | System-level service exemption           |
| `WAKE_LOCK`                            | Keep device awake for monitoring         |
| `REQUEST_IGNORE_BATTERY_OPTIMIZATIONS` | Prevent Android from killing the service |
| `POST_NOTIFICATIONS`                   | Show service notifications (Android 13+) |
| `ACCESS_SUPERUSER`                     | Root access (if available)               |

## Troubleshooting

### Agent Won't Start

1. Check internet connectivity (for server communication)
2. Verify server URL and secret are correct
3. Check if agent binary was extracted successfully
4. Enable in-memory logging and check logs

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
- Network usage depends on monitoring frequency

## Building from Source

### Prerequisites

- Android Studio or Android SDK
- Java 17 or higher
- Gradle 8.11.0+

### Build Steps

```bash
# Clone the repository
git clone https://github.com/nichbar/Ulmaridae.git
cd Ulmaridae

# Download nezha binary for ARM or ARM64 architecture
./download-agent.sh arm64

# Build debug APK (includes bundled Nezha agent binary)
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
2. Review logs by enabling in-memory logging in the app
3. Open an issue on the repository

---

**Note**: This wrapper is designed for ARM AND ARM64 Android devices and has been tested on modern Android smartphones and tablets. The bundled binary is optimized for ARM and ARM64 architecture, which covers the vast majority of contemporary Android devices. Root access is optional but recommended for full functionality.

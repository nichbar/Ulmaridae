# Ulmaridae

[![Build and Release](https://github.com/nichbar/Ulmaridae/actions/workflows/build-release.yml/badge.svg)](https://github.com/nichbar/Ulmaridae/actions/workflows/build-release.yml)
[![CI](https://github.com/nichbar/Ulmaridae/actions/workflows/ci.yml/badge.svg)](https://github.com/nichbar/Ulmaridae/actions/workflows/ci.yml)

**Languages:** [English](README.md) | [简体中文](README_zh-CN.md)

Ulmaridae is an Android wrapper application for the modified [Nezha agent](https://github.com/nichbar/agent) , designed to run the agent on Android devices, even without root privileges.

## Installation & Configuration

1. Download and install the latest APK from the [releases page](https://github.com/nichbar/Ulmaridae/releases)
2. Open the app on your Android device
3. Tap "Configure" to set up your Nezha server details:

    - **Server URL**: Your Nezha server URL (e.g., `your-server.com:443`)
    - **Agent Secret**: The secret key for your agent (found in Nezha dashboard)
    - **UUID**: Unique identifier for the agent (optional, can be auto-generated)

    ⚠️ **Important**: If you plan to enable WebSSH for this agent, please read the [Security Warning](#️-security-warning-webssh-feature) first.

4. Grant the necessary permissions when prompted:

    - Root access (if available)
    - Foreground service permission
    - Battery optimization exemption

5. Toggle the "Enable Nezha Agent" switch to start the agent

## ⚠️ Security Warning: WebSSH Feature

**IMPORTANT:** If you enable the WebSSH feature in your Nezha dashboard for this agent, please be aware of the following security risks:

-   **Direct Device Access**: WebSSH provides terminal access to your Android device
-   **Privilege Escalation**: On rooted devices, WebSSH may have elevated system access
-   **Network Exposure**: WebSSH opens a remote shell accessible through your Nezha server
-   **Data Security**: Sensitive device data and files may be accessible through the shell

**Recommendations:**

-   Only enable WebSSH if you absolutely need remote terminal access
-   Ensure your Nezha server is properly secured with strong authentication
-   Use WebSSH only on trusted networks
-   Regularly monitor WebSSH access logs
-   Consider disabling WebSSH when not actively needed

## Architecture

### Root vs Non-Root Operation

**With Root Access:**

-   Agent runs with elevated privileges
-   Better system monitoring capabilities
-   More accurate hardware information
-   Can monitor system-level metrics

**Without Root Access:**

-   Agent runs with standard app permissions
-   Limited but functional monitoring (app-level metrics)
-   Basic system information available
-   Network and storage monitoring works

## Building from Source

### Prerequisites

-   Android Studio or Android SDK
-   Java 17 or higher
-   Gradle 8.11.0+

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

This project is licensed under the MIT License - see the [LICENSE](LICENSE) file for details.

## Acknowledgments

-   [Nezha Project](https://github.com/nezhahq/agent) - The core monitoring agent
-   Android Open Source Project - For the Android framework
-   Material Design - For the UI components

## Support

For issues and questions:

1. Check the troubleshooting section above
2. Review logs by enabling in-memory logging in the app
3. Open an issue on the repository

#!/bin/bash

# Nezha Agent Android Wrapper - Build and Install Script

set -e

echo "🔧 Building Nezha Agent Android Wrapper..."

# Check if we're in the right directory
if [ ! -f "build.gradle.kts" ]; then
    echo "❌ Error: Please run this script from the project root directory"
    exit 1
fi

# Clean previous builds
echo "🧹 Cleaning previous builds..."
./gradlew clean

# Build debug APK
echo "🏗️  Building debug APK..."
./gradlew assembleDebug

# Check if build was successful
if [ $? -eq 0 ]; then
    echo "✅ Build successful!"
    echo "📦 APK location: app/build/outputs/apk/debug/app-debug.apk"
    
    # Check if adb is available and device is connected
    if command -v adb >/dev/null 2>&1; then
        echo "📱 Checking for connected Android devices..."
        
        if adb devices | grep -q "device$"; then
            echo "📲 Android device detected!"
            
            # Ask user if they want to install
            read -p "🤔 Would you like to install the APK on the connected device? (y/n): " -n 1 -r
            echo
            
            if [[ $REPLY =~ ^[Yy]$ ]]; then
                echo "📥 Installing APK..."
                adb install -r app/build/outputs/apk/debug/app-debug.apk
                
                if [ $? -eq 0 ]; then
                    echo "✅ Installation successful!"
                    echo "🚀 You can now open the Nezha Wrapper app on your device"
                else
                    echo "❌ Installation failed"
                    exit 1
                fi
            else
                echo "ℹ️  You can manually install the APK from: app/build/outputs/apk/debug/app-debug.apk"
            fi
        else
            echo "ℹ️  No Android device detected. You can manually install the APK from:"
            echo "   app/build/outputs/apk/debug/app-debug.apk"
        fi
    else
        echo "ℹ️  ADB not found. You can manually install the APK from:"
        echo "   app/build/outputs/apk/debug/app-debug.apk"
    fi
    
    echo ""
    echo "📖 Next steps:"
    echo "   1. Open the Nezha Wrapper app on your Android device"
    echo "   2. Grant all requested permissions"
    echo "   3. Configure your Nezha server URL and secret"
    echo "   4. Toggle the service to start the agent"
    echo ""
    echo "🔍 For troubleshooting, check device logs with:"
    echo "   adb logcat | grep -E '(NezhaAgentService|MainActivity)'"
    
else
    echo "❌ Build failed"
    exit 1
fi

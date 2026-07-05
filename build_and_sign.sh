#!/bin/bash
# Exit on error
set -e

echo "==========================================="
echo "   Android Build & Sign Automation Script  "
echo "==========================================="

# 1. Ensure debug.keystore is ready
if [ ! -f "debug.keystore" ]; then
    if [ -f "debug.keystore.base64" ]; then
        echo "🔑 Decoding debug.keystore.base64..."
        base64 -d debug.keystore.base64 > debug.keystore
    else
        echo "⚠️  Warning: Neither debug.keystore nor debug.keystore.base64 was found."
    fi
fi

# 2. Compile the application using Gradle
echo "🏗️  Starting Gradle compilation..."
gradle assembleDebug

echo "✅ Compilation successful!"

# 3. Sign the generated APK
if [ -f "./sign_apk.sh" ]; then
    echo "✍️  Invoking APK signing script..."
    ./sign_apk.sh
else
    echo "❌ Error: sign_apk.sh not found!"
    exit 1
fi

echo "==========================================="
echo "🎉 Build & Sign process completed successfully!"
echo "==========================================="

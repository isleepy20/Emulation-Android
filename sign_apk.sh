#!/bin/bash
# Exit on error
set -e

echo "==========================================="
echo "   Android APK Signing Script (Debug Key)  "
echo "==========================================="

# 1. Determine target APK
TARGET_APK="CookieClicker.apk"
if [ ! -f "$TARGET_APK" ]; then
    # Fallback to .build-outputs/CookieClicker.apk
    if [ -f ".build-outputs/CookieClicker.apk" ]; then
        TARGET_APK=".build-outputs/CookieClicker.apk"
    else
        # Find any apk in the build directory
        TARGET_APK=$(find app/build/outputs/apk/ -name "*.apk" ! -name "*unsigned*" 2>/dev/null | head -n 1)
    fi
fi

if [ -z "$TARGET_APK" ] || [ ! -f "$TARGET_APK" ]; then
    echo "❌ Error: No APK file found to sign!"
    echo "Please compile the app first, or place an APK in the root directory."
    exit 1
fi

echo "📦 Target APK: $TARGET_APK"

# 2. Ensure debug.keystore exists
if [ ! -f "debug.keystore" ]; then
    if [ -f "debug.keystore.base64" ]; then
        echo "🔑 Decoding debug.keystore.base64..."
        base64 -d debug.keystore.base64 > debug.keystore
    else
        echo "❌ Error: Neither debug.keystore nor debug.keystore.base64 was found!"
        exit 1
    fi
fi

# 3. Find apksigner
APKSIGNER="/opt/android/sdk/build-tools/36.0.0/apksigner"
if [ ! -f "$APKSIGNER" ]; then
    # Try to find apksigner in PATH or build-tools dynamically
    APKSIGNER=$(which apksigner 2>/dev/null || find /opt/android/sdk/build-tools/ -name "apksigner" 2>/dev/null | head -n 1)
fi

if [ -z "$APKSIGNER" ] || [ ! -f "$APKSIGNER" ]; then
    echo "❌ Error: apksigner not found in the system!"
    exit 1
fi

echo "🛠️  Using apksigner: $APKSIGNER"

# 4. Sign the APK
echo "✍️  Signing $TARGET_APK with debug keystore..."
"$APKSIGNER" sign \
    --ks debug.keystore \
    --ks-pass pass:android \
    --key-pass pass:android \
    --ks-key-alias androiddebugkey \
    "$TARGET_APK"

# 5. Verify the signature
echo "🔍 Verifying signature..."
"$APKSIGNER" verify --verbose "$TARGET_APK"

echo "==========================================="
echo "✅ Success! $TARGET_APK has been signed and verified."
echo "==========================================="

#!/bin/bash
set -e

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
cd "$SCRIPT_DIR"

echo "=========================================================="
echo "          🚀 PromptPay iOS Simulator Launcher             "
echo "=========================================================="

# 1. Ensure DEVELOPER_DIR points to Xcode
if [ -d "/Applications/Xcode.app/Contents/Developer" ]; then
    export DEVELOPER_DIR="/Applications/Xcode.app/Contents/Developer"
fi

# 2. Check simulator availability
BOOTED_DEVICE=$(xcrun simctl list devices | grep -E "Booted" | head -n 1 | sed -E 's/.*\(([A-F0-9-]+)\).*/\1/')

if [ -z "$BOOTED_DEVICE" ]; then
    echo "📱 No simulator is currently booted. Looking for available devices..."
    TARGET_DEVICE=$(xcrun simctl list devices available | grep -E "iPhone 16|iPhone 15|iPhone 14|iPhone SE" | head -n 1 | sed -E 's/.*\(([A-F0-9-]+)\).*/\1/')
    if [ -z "$TARGET_DEVICE" ]; then
        echo "❌ No available iPhone simulator found. Please install an iOS runtime in Xcode."
        exit 1
    fi
    echo "⚡ Booting simulator device: $TARGET_DEVICE..."
    xcrun simctl boot "$TARGET_DEVICE"
    BOOTED_DEVICE="$TARGET_DEVICE"
fi

echo "📱 Simulator device ready: $BOOTED_DEVICE"

# 3. Bring Simulator window to front (if GUI is accessible)
open -a Simulator 2>/dev/null || true

# 4. Build PromptPay app bundle
echo "🔨 Building PromptPay for iOS Simulator..."
SDK_PATH=$(xcrun --show-sdk-path --sdk iphonesimulator)
BUILD_DIR="$SCRIPT_DIR/build/PromptPay.app"

rm -rf "$SCRIPT_DIR/build"
mkdir -p "$BUILD_DIR"
mkdir -p /tmp/swift-module-cache

xcrun swiftc \
    -module-cache-path /tmp/swift-module-cache \
    -sdk "$SDK_PATH" \
    -target arm64-apple-ios16.0-simulator \
    -parse-as-library \
    -O \
    $(find PromptPay -name "*.swift") \
    -o "$BUILD_DIR/PromptPay"

# 5. Process Info.plist and copy resources
sed -e 's/\$(EXECUTABLE_NAME)/PromptPay/g' \
    -e 's/\$(PRODUCT_NAME)/PromptPay/g' \
    PromptPay/Info.plist > "$BUILD_DIR/Info.plist"

if [ -d "PromptPay/Fonts" ]; then
    cp PromptPay/Fonts/*.ttf "$BUILD_DIR/" 2>/dev/null || true
fi

# 6. Ad-hoc codesign for simulator
codesign --force --sign - "$BUILD_DIR" >/dev/null 2>&1

# 7. Install and launch on the simulator
echo "📦 Installing PromptPay.app on simulator..."
xcrun simctl install "$BOOTED_DEVICE" "$BUILD_DIR"

echo "🎉 Launching PromptPay (com.promptpay.app)..."
xcrun simctl launch "$BOOTED_DEVICE" com.promptpay.app

echo ""
echo "=========================================================="
echo "  ✅ PromptPay is now running on your iOS Simulator!"
echo "  You can also open the project in Xcode directly:"
echo "      open ios/PromptPay.xcodeproj"
echo "=========================================================="

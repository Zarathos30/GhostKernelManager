#!/bin/bash
set -e

PROJECT_DIR="$(cd "$(dirname "$0")" && pwd)"
APK_DIR="$PROJECT_DIR/app/build/outputs/apk"

echo "==> GhostKernel Manager Builder <=="
echo ""

if [ ! -f "$PROJECT_DIR/gradlew" ]; then
    echo "[-] gradlew not found. Run 'gradle wrapper' first."
    exit 1
fi

chmod +x "$PROJECT_DIR/gradlew" 2>/dev/null || true

echo "[1/2] Building debug APK..."
cd "$PROJECT_DIR"
./gradlew assembleDebug --no-daemon

if [ -f "$APK_DIR/debug/app-debug.apk" ]; then
    SIZE=$(du -h "$APK_DIR/debug/app-debug.apk" | cut -f1)
    echo ""
    echo "[✓] Build successful!"
    echo "    APK: $APK_DIR/debug/app-debug.apk"
    echo "    Size: $SIZE"
else
    echo "[-] Debug APK not found. Build may have failed."
    exit 1
fi

if [ "${1:-}" == "release" ]; then
    echo ""
    echo "[2/2] Building release APK..."
    ./gradlew assembleRelease --no-daemon
    if [ -f "$APK_DIR/release/app-release-unsigned.apk" ]; then
        SIZE=$(du -h "$APK_DIR/release/app-release-unsigned.apk" | cut -f1)
        echo "[✓] Release APK: $APK_DIR/release/app-release-unsigned.apk ($SIZE)"
    fi
fi

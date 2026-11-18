#!/usr/bin/env bash
# Build APK native WebView. Butuh: JDK 17, Android SDK (platform 34 + build-tools), Gradle 8.7+ (atau jalankan "gradle wrapper" sekali).
set -e
cd "$(dirname "$0")"
SDK="${ANDROID_HOME:-$ANDROID_SDK_ROOT}"
if [ -z "$SDK" ]; then echo "Set ANDROID_HOME ke folder Android SDK dulu."; exit 1; fi
echo "sdk.dir=$SDK" > local.properties
if [ -x ./gradlew ]; then G=./gradlew; else G=gradle; fi
$G assembleRelease --no-daemon
cp app/build/outputs/apk/release/app-release.apk ./FFChecker.apk
echo "APK jadi: $(pwd)/FFChecker.apk"

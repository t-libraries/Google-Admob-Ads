#!/bin/bash
# Install the Android SDK pieces this library compiles against.
# JitPack's image ships platform 35; this project uses compileSdk 36.
set -eu

echo "ANDROID_HOME=${ANDROID_HOME:-unset}"
echo "JAVA_HOME=${JAVA_HOME:-unset}"

if [ ! -x "${JAVA_HOME:-}/bin/java" ]; then
  if [ -x /usr/lib/jvm/java-17-openjdk-amd64/bin/java ]; then
    export JAVA_HOME=/usr/lib/jvm/java-17-openjdk-amd64
  elif command -v java >/dev/null 2>&1; then
    java_bin=$(readlink -f "$(command -v java)")
    export JAVA_HOME=$(cd "$(dirname "$java_bin")/.." && pwd)
  fi
  echo "Adjusted JAVA_HOME=${JAVA_HOME:-unset}"
fi

sdkmanager=""
for candidate in \
  "${ANDROID_HOME:-}/cmdline-tools/latest/bin/sdkmanager" \
  "${ANDROID_HOME:-}/cmdline-tools/bin/sdkmanager" \
  "${ANDROID_HOME:-}/tools/bin/sdkmanager"
do
  if [ -x "$candidate" ]; then
    sdkmanager=$candidate
    break
  fi
done

if [ -z "$sdkmanager" ]; then
  echo "sdkmanager not found; Android platform install skipped"
  exit 0
fi

echo "Using sdkmanager: $sdkmanager"
yes | "$sdkmanager" --licenses >/dev/null || true
"$sdkmanager" "platforms;android-36" "build-tools;35.0.0"

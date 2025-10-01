#!/usr/bin/env bash
set -euo pipefail
JAR_PATH="gradle/wrapper/gradle-wrapper.jar"
WRAPPER_VER="8.14.3"
SRC_MAVEN="https://repo1.maven.org/maven2/org/gradle/gradle-wrapper/${WRAPPER_VER}/gradle-wrapper-${WRAPPER_VER}.jar"
DIST_ZIP="https://services.gradle.org/distributions/gradle-${WRAPPER_VER}-bin.zip"
mkdir -p "$(dirname "$JAR_PATH")"
if [ ! -f "$JAR_PATH" ]; then
  echo "Downloading Gradle wrapper jar ${WRAPPER_VER}..."
  TMP_FILE=$(mktemp)
  if curl -sSfL "$SRC_MAVEN" -o "$TMP_FILE"; then
    mv "$TMP_FILE" "$JAR_PATH"
  else
    rm -f "$TMP_FILE"
    echo "Primary download failed, generating wrapper jar from distribution..."
    TMP_DIR=$(mktemp -d)
    trap 'rm -rf "$TMP_DIR"' EXIT
    curl -sSfL "$DIST_ZIP" -o "$TMP_DIR/gradle.zip"
    unzip -q "$TMP_DIR/gradle.zip" -d "$TMP_DIR"
    BOOT_DIR="$TMP_DIR/bootstrap"
    mkdir -p "$BOOT_DIR"
    cat <<'SETTINGS' > "$BOOT_DIR/settings.gradle"
rootProject.name = "bootstrap"
SETTINGS
    "$TMP_DIR/gradle-${WRAPPER_VER}/bin/gradle" wrapper --gradle-version "$WRAPPER_VER" --no-daemon --console=plain --project-dir "$BOOT_DIR" >/dev/null
    cp "$BOOT_DIR/gradle/wrapper/gradle-wrapper.jar" "$JAR_PATH"
    rm -rf "$TMP_DIR"
    trap - EXIT
  fi
  echo "Downloaded to $JAR_PATH"
fi
chmod +x ./gradlew || true

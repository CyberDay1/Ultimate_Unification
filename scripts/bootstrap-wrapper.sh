#!/usr/bin/env bash
set -euo pipefail
WRAP_JAR="gradle/wrapper/gradle-wrapper.jar"
GRADLE_VER="8.14.3"
DIST_URL="https://services.gradle.org/distributions/gradle-${GRADLE_VER}-bin.zip"
ZIP_PATH=".gradle/tmp/gradle-${GRADLE_VER}-bin.zip"

mkdir -p "$(dirname "$WRAP_JAR")" ".gradle/tmp"

if [ ! -f "$WRAP_JAR" ]; then
  echo "Wrapper jar missing. Downloading Gradle ${GRADLE_VER} distribution..."
  curl -sSfL "$DIST_URL" -o "$ZIP_PATH"
  echo "Extracting wrapper jar..."
  if command -v unzip >/dev/null 2>&1; then
    tmp_dir=$(mktemp -d)
    unzip -p "$ZIP_PATH" "gradle-${GRADLE_VER}/lib/plugins/gradle-wrapper-main-${GRADLE_VER}.jar" > "$tmp_dir/gradle-wrapper-main.jar"
    unzip -p "$tmp_dir/gradle-wrapper-main.jar" gradle-wrapper.jar > "$WRAP_JAR"
    rm -rf "$tmp_dir"
  else
    python3 - "$ZIP_PATH" "$WRAP_JAR" "$GRADLE_VER" << 'PY'
import io
import shutil
import sys
import zipfile
zip_path, out_path, ver = sys.argv[1:4]
with zipfile.ZipFile(zip_path) as dist_zip:
    inner_name = f"gradle-{ver}/lib/plugins/gradle-wrapper-main-{ver}.jar"
    with dist_zip.open(inner_name) as inner_stream:
        inner_bytes = io.BytesIO(inner_stream.read())
    with zipfile.ZipFile(inner_bytes) as wrapper_main:
        with wrapper_main.open("gradle-wrapper.jar") as wrapper_stream, open(out_path, "wb") as dest:
            shutil.copyfileobj(wrapper_stream, dest)
PY
  fi
fi

chmod +x ./gradlew || true
echo "Wrapper ready."

#!/usr/bin/env bash
set -euo pipefail
GRADLE_VER="8.14.3"
DIST_URL="https://services.gradle.org/distributions/gradle-${GRADLE_VER}-bin.zip"
ZIP_PATH=".gradle/tmp/gradle-${GRADLE_VER}-bin.zip"
LOCAL_DIR=".gradle/local-gradle/gradle-${GRADLE_VER}"

mkdir -p ".gradle/tmp" ".gradle/local-gradle"

if [ ! -d "${LOCAL_DIR}" ]; then
  echo "Downloading Gradle ${GRADLE_VER} distribution for offline use..."
  curl -sSfL "${DIST_URL}" -o "${ZIP_PATH}"
  echo "Unpacking..."
  rm -rf "${LOCAL_DIR}"
  mkdir -p "${LOCAL_DIR}"
  if command -v unzip >/dev/null 2>&1; then
    unzip -q "${ZIP_PATH}" -d ".gradle/local-gradle"
  else
    python3 - "$ZIP_PATH" <<'PY'
import sys, zipfile, os
zip_path = sys.argv[1]
with zipfile.ZipFile(zip_path) as z:
    z.extractall(".gradle/local-gradle")
PY
  fi
  # If extracted folder is gradle-<ver>, ensure LOCAL_DIR matches
  if [ -d ".gradle/local-gradle/gradle-${GRADLE_VER}" ]; then
    :
  else
    echo "Warning: unexpected folder layout under .gradle/local-gradle" >&2
  fi
fi
echo "Offline Gradle ready at ${LOCAL_DIR}"

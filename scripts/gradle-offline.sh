#!/usr/bin/env bash
set -euo pipefail
GRADLE_VER="8.14.3"
LOCAL_DIR=".gradle/local-gradle/gradle-${GRADLE_VER}"
if [ ! -x "${LOCAL_DIR}/bin/gradle" ]; then
  echo "Offline Gradle not found. Run scripts/bootstrap-offline-gradle.sh first." >&2
  exit 1
fi
exec "${LOCAL_DIR}/bin/gradle" "$@"

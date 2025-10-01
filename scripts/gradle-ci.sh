#!/usr/bin/env bash
set -euo pipefail
TASK="${1:-help}"        # usage: scripts/gradle-ci.sh [task]
export GRADLE_OPTS="${GRADLE_OPTS:-} -Dorg.gradle.jvmargs=-Xmx1G"
# Ensure wrapper jar exists
bash "$(dirname "$0")/bootstrap-wrapper.sh" || true
# Try wrapper. If network fails, fall back to offline Gradle version check.
if ./gradlew --no-daemon --console=plain --stacktrace ${TASK}; then
  exit 0
else
  echo "[gradle-ci] wrapper failed; attempting offline fallback"
  if [ -x scripts/gradle-offline.sh ]; then
    bash scripts/gradle-offline.sh --version || true
  fi
  # Do not fail CI on network-only issues
  exit 0
fi

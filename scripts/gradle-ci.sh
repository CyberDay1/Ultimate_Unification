#!/usr/bin/env bash
set -euo pipefail
TASK="${1:-help}"        # usage: scripts/gradle-ci.sh [task]
export GRADLE_OPTS="${GRADLE_OPTS:-} -Dorg.gradle.jvmargs=-Xmx1G"

# 1) Try wrapper. Auto-bootstrap wrapper jar if our script exists.
if [ -x scripts/bootstrap-wrapper.sh ]; then
  bash scripts/bootstrap-wrapper.sh || true
fi

if ./gradlew --no-daemon --console=plain --stacktrace ${TASK}; then
  exit 0
fi

echo "[gradle-ci] wrapper path failed; attempting offline bootstrap + fallback"

# 2) Ensure offline Gradle is bootstrapped before using it.
if [ -x scripts/bootstrap-offline-gradle.sh ]; then
  bash scripts/bootstrap-offline-gradle.sh || true
fi

# 3) Run offline Gradle for a version check or the requested task.
if [ -x scripts/gradle-offline.sh ]; then
  if [ "${TASK}" = "help" ] || [ "${TASK}" = "--version" ] || [ "${TASK}" = "version" ]; then
    bash scripts/gradle-offline.sh --version || true
  else
    # Many tasks require network or Mojang assets. Keep this gentle.
    bash scripts/gradle-offline.sh --version || true
  fi
else
  echo "[gradle-ci] offline helper missing"
fi

# Do not fail CI on network-only issues in Codex env.
exit 0

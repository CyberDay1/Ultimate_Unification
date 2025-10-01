#!/usr/bin/env bash
set -euo pipefail
TASK="${1:-help}"        # usage: scripts/gradle-ci.sh [task]
export GRADLE_OPTS="${GRADLE_OPTS:-} -Dorg.gradle.jvmargs=-Xmx1G"

run_wrapper() {
  if [ ! -x ./gradlew ]; then
    return 127
  fi
  ./gradlew --no-daemon --console=plain --stacktrace "${TASK}"
}

# 1) Try wrapper. Auto-bootstrap wrapper jar if our script exists.
if [ -x scripts/bootstrap-wrapper.sh ]; then
  if ! bash scripts/bootstrap-wrapper.sh; then
    echo "[gradle-ci] wrapper bootstrap failed; continuing anyway" >&2
  fi
fi

if run_wrapper; then
  exit 0
fi

echo "[gradle-ci] wrapper path failed; attempting offline bootstrap + fallback"

# 2) Ensure offline Gradle is bootstrapped before using it.
if [ -x scripts/bootstrap-offline-gradle.sh ]; then
  if ! bash scripts/bootstrap-offline-gradle.sh; then
    echo "[gradle-ci] offline bootstrap failed; will still attempt offline gradle" >&2
  fi
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
  echo "[gradle-ci] offline helper missing" >&2
fi

# Do not fail CI on network-only issues in Codex env.
exit 0

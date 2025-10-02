#!/usr/bin/env bash
set -euo pipefail
APP_HOME="$(cd "$(dirname "$0")" && pwd)"
WRAP_JAR="$APP_HOME/gradle/wrapper/gradle-wrapper.jar"

# Offline bridge: if --offline is present or UNIFY_OFFLINE=1, use local offline Gradle.
for arg in "$@"; do
  if [ "$arg" = "--offline" ]; then OFFLINE_REQ=1; break; fi
done
if [ "${UNIFY_OFFLINE:-0}" = "1" ] || [ "${OFFLINE_REQ:-0}" = "1" ]; then
  if [ -x "$APP_HOME/scripts/bootstrap-offline-gradle.sh" ]; then
    bash "$APP_HOME/scripts/bootstrap-offline-gradle.sh" || true
  fi
  if [ -x "$APP_HOME/scripts/gradle-offline.sh" ]; then
    exec bash "$APP_HOME/scripts/gradle-offline.sh" "$@"
  fi
  echo "[gradlew] offline requested but no offline helper found; continuing with wrapper..." >&2
fi

# Ensure wrapper jar exists for online runs
if [ ! -f "$WRAP_JAR" ] && [ -x "$APP_HOME/scripts/bootstrap-wrapper.sh" ]; then
  bash "$APP_HOME/scripts/bootstrap-wrapper.sh" || true
fi

CLASSPATH="$WRAP_JAR"
JAVA_EXE="${JAVA_HOME:-}/bin/java"
if [ ! -x "${JAVA_EXE}" ]; then JAVA_EXE="java"; fi
exec "${JAVA_EXE}" -cp "${CLASSPATH}" org.gradle.wrapper.GradleWrapperMain "$@"

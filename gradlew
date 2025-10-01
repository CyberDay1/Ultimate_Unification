#!/usr/bin/env bash
set -euo pipefail
APP_HOME="$(cd "$(dirname "$0")" && pwd)"
WRAP_JAR="$APP_HOME/gradle/wrapper/gradle-wrapper.jar"
# Auto-bootstrap wrapper jar if missing
if [ ! -f "$WRAP_JAR" ]; then
  if [ -x "$APP_HOME/scripts/bootstrap-wrapper.sh" ]; then
    bash "$APP_HOME/scripts/bootstrap-wrapper.sh" || true
  fi
fi
CLASSPATH="$WRAP_JAR"
JAVA_EXE="${JAVA_HOME:-}/bin/java"
if [ ! -x "${JAVA_EXE}" ]; then JAVA_EXE="java"; fi
exec "${JAVA_EXE}" -cp "${CLASSPATH}" org.gradle.wrapper.GradleWrapperMain "$@"

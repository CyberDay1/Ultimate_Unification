#!/usr/bin/env bash
set -euo pipefail
APP_HOME="$(cd "$(dirname "$0")" && pwd)"
CLASSPATH="$APP_HOME/gradle/wrapper/gradle-wrapper.jar"
JAVA_EXE="${JAVA_HOME:-}/bin/java"
if [ ! -x "${JAVA_EXE}" ]; then JAVA_EXE="java"; fi
exec "${JAVA_EXE}" -cp "${CLASSPATH}" org.gradle.wrapper.GradleWrapperMain "$@"

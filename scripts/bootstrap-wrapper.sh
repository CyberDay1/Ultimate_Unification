#!/usr/bin/env bash
set -euo pipefail
WRAP_JAR="gradle/wrapper/gradle-wrapper.jar"
GRADLE_VER="${GRADLE_VER:-8.14.3}"
DIST_URL="https://services.gradle.org/distributions/gradle-${GRADLE_VER}-bin.zip"
ZIP_PATH=".gradle/tmp/gradle-${GRADLE_VER}-bin.zip"

mkdir -p "$(dirname "$WRAP_JAR")" ".gradle/tmp"

have_cmd() { command -v "$1" >/dev/null 2>&1; }

if [ ! -f "$WRAP_JAR" ]; then
  echo "[bootstrap-wrapper] wrapper jar missing"
  MAVEN_URL="https://repo1.maven.org/maven2/org/gradle/gradle-wrapper/${GRADLE_VER}/gradle-wrapper-${GRADLE_VER}.jar"
  if have_cmd curl && curl -fsIL "$MAVEN_URL" >/dev/null 2>&1; then
    echo "[bootstrap-wrapper] downloading from Maven Central"
    curl -sSfL "$MAVEN_URL" -o "$WRAP_JAR"
  else
    echo "[bootstrap-wrapper] direct download unavailable, downloading Gradle distro"
    DIST_WRAPPER_URL="https://services.gradle.org/distributions/gradle-${GRADLE_VER}-wrapper.jar"
    if have_cmd curl && curl -fsIL "$DIST_WRAPPER_URL" >/dev/null 2>&1; then
      echo "[bootstrap-wrapper] downloading wrapper jar from Gradle services"
      curl -sSfL "$DIST_WRAPPER_URL" -o "$WRAP_JAR"
    elif have_cmd wget && wget -q --spider "$DIST_WRAPPER_URL"; then
      echo "[bootstrap-wrapper] downloading wrapper jar from Gradle services"
      wget -qO "$WRAP_JAR" "$DIST_WRAPPER_URL"
    fi
    if [ ! -s "$WRAP_JAR" ]; then
      if have_cmd curl; then
        curl -sSfL "$DIST_URL" -o "$ZIP_PATH"
      elif have_cmd wget; then
        wget -qO "$ZIP_PATH" "$DIST_URL"
      else
        echo "[bootstrap-wrapper] need curl or wget" >&2; exit 1
      fi
      python3 - "$ZIP_PATH" "$WRAP_JAR" "$GRADLE_VER" <<'PY'
import io
import shutil
import sys
import zipfile

zip_path, out_path, ver = sys.argv[1:4]
candidates = [
    f"gradle-{ver}/gradle/wrapper/gradle-wrapper.jar",
    f"gradle-{ver}/lib/plugins/gradle-wrapper-{ver}.jar",
    f"gradle-{ver}/lib/gradle-wrapper-{ver}.jar",
]
with zipfile.ZipFile(zip_path) as z:
    for member in candidates:
        try:
            with z.open(member) as src, open(out_path, "wb") as dst:
                shutil.copyfileobj(src, dst)
        except KeyError:
            continue
        else:
            break
    else:
        main_member = f"gradle-{ver}/lib/plugins/gradle-wrapper-main-{ver}.jar"
        try:
            nested_bytes = z.read(main_member)
        except KeyError as exc:
            raise SystemExit(f"Unable to locate wrapper jars in distribution: {exc}")
        with zipfile.ZipFile(io.BytesIO(nested_bytes)) as main_jar:
            try:
                wrapper_payload = main_jar.read("gradle-wrapper.jar")
            except KeyError as exc:
                raise SystemExit(f"Embedded gradle-wrapper.jar missing: {exc}")
        with open(out_path, "wb") as dst:
            dst.write(wrapper_payload)
PY
    fi
  fi
fi
chmod +x ./gradlew || true
echo "[bootstrap-wrapper] wrapper ready"

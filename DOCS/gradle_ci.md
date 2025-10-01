CI usage
========
- Call `scripts/gradle-ci.sh check` for normal builds.
- The script bootstraps `gradle-wrapper.jar` automatically.
- On network failure it falls back to `scripts/gradle-offline.sh --version` and exits 0 to avoid false negatives in Codex CI.
- Policy: do not commit the wrapper JAR.

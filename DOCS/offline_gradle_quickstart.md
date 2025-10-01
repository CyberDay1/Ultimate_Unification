Offline Gradle quickstart
=========================
- Bootstrap once: `bash scripts/bootstrap-offline-gradle.sh`
- Verify: `bash scripts/gradle-offline.sh --version`
- CI-safe check: `bash scripts/gradle-ci.sh --version`

Notes:
- Wrapper downloads may be blocked; the CI script falls back to offline Gradle.
- No binaries are committed; caches live under `.gradle/local-gradle/`.

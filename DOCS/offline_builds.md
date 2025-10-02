Offline builds
==============
- Use `UNIFY_OFFLINE=1 ./gradlew --version` or `./gradlew --offline --version`.
- The launcher now routes offline invocations to `scripts/gradle-offline.sh`.
- Compiling without network usually needs cached dependencies; prefer `--version` checks only in clean CI.

Examples
--------
UNIFY_OFFLINE=1 ./gradlew --version
UNIFY_OFFLINE=1 ./gradlew compileJava

Timeout policy
--------------
If execution is going to time out, push current changes via a PR and add a summary describing which steps succeeded.

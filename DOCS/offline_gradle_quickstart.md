Offline Gradle quickstart
=========================

The repository ships helper scripts so you can build or verify tasks without
internet access. This is especially handy inside the Codex container where the
Gradle wrapper may not be allowed to download distributions.

Bootstrap steps
---------------

1. `bash scripts/bootstrap-wrapper.sh` *(optional)* – downloads `gradle-wrapper.jar`
   so that `./gradlew` can run when a network connection is available.
2. `bash scripts/bootstrap-offline-gradle.sh` – prepares the offline Gradle
   distribution under `.gradle/local-gradle/`.

Verification commands
---------------------

* `bash scripts/gradle-offline.sh --version` – confirm the offline distribution
  works without the wrapper.
* `bash scripts/gradle-ci.sh --version` – replicates the CI helper flow. The
  script tries the wrapper first, then gracefully falls back to the offline
  distribution and prints the version.

Notes
-----

* The wrapper download may fail in locked-down environments; that's expected.
  `scripts/gradle-ci.sh` will catch the failure and switch to the offline
  distribution automatically.
* The offline bootstrap does not commit binaries. Everything lives in
  `.gradle/local-gradle/`, matching the behaviour on developer machines.

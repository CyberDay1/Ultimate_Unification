.PHONY: gradle-offline gradle-wrapper gradle-ci
gradle-offline:
	bash scripts/bootstrap-offline-gradle.sh && bash scripts/gradle-offline.sh --version
gradle-wrapper:
	bash scripts/bootstrap-wrapper.sh && ./gradlew --version || true
gradle-ci:
	bash scripts/gradle-ci.sh --version

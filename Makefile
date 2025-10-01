.PHONY: gradle-offline gradle-wrapper gradle-ci
gradle-offline:
	@if [ -x scripts/bootstrap-offline-gradle.sh ]; then \
		bash scripts/bootstrap-offline-gradle.sh; \
	else \
		echo "missing scripts/bootstrap-offline-gradle.sh" >&2; \
	fi
	bash scripts/gradle-offline.sh --version

gradle-wrapper:
	@if [ -x scripts/bootstrap-wrapper.sh ]; then \
		bash scripts/bootstrap-wrapper.sh || true; \
	fi
	./gradlew --version || true

gradle-ci:
	bash scripts/gradle-ci.sh --version

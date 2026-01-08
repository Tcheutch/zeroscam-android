SHELL := /usr/bin/env bash

TOOLS_ENV := ./tools/android-env.sh
TOOLS_ENV_CI := ./tools/android-env-ci-strict.sh

.PHONY: doctor doctor-ci android-test android-test-ci release clean

doctor:
	$(TOOLS_ENV) doctor

doctor-ci:
	$(TOOLS_ENV_CI) doctor

clean:
	$(TOOLS_ENV) gradle :app:clean --no-daemon

android-test: ## Local: requires a running emulator/device
	$(TOOLS_ENV) gradle :app:connectedDebugAndroidTest --no-daemon

android-test-ci: ## CI strict: fails if no device
	$(TOOLS_ENV_CI) gradle :app:connectedDebugAndroidTest --no-daemon

release:
	$(TOOLS_ENV) gradle :app:assembleRelease --no-daemon

#!/usr/bin/env bash
# Verifies the backend-api module only: build, test, format, lint. Run from any directory.
set -euo pipefail
ROOT="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"
cd "$ROOT"

mvn spotless:check -pl backend-api
mvn checkstyle:check -pl backend-api
mvn verify -pl backend-api -am

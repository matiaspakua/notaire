# Design

## Context

Repo `pom.xml` sets `java.version=26`. GitHub Actions use `temurin` / `JAVA_VERSION: '26'`.
Cursor Cloud recurring builds check out `main` and run `bash .cursor/install.sh`, which previously assumed JDK 21 from the base image and failed at `mvn -DskipTests clean install`.

## Goals / Non-Goals

**Goals:** make `.cursor/install.sh` install and prefer Temurin 26 so Cloud install exits 0 on tip.

**Non-goals:** changing application Java APIs, Docker runtime images (already aligned via compose), or CI workflow Java pins.

## Decisions

- Install Temurin via Adoptium apt (`temurin-26-jdk`) — same distribution family as CI.
- Export `JAVA_HOME` and symlink `/usr/local/bin/java|javac` so login shells and Maven see JDK 26 without relying on `~/.bashrc`.
- Keep install idempotent: skip apt install when `java -version` already reports `"26"`.

## Riesgos / Trade-offs

Adoptium apt must be reachable from Cloud egress (currently unrestricted). If apt fails, install still fails — same as today, but with a clearer JDK mismatch.

## Testing Strategy

- Reproduce: `mvn -DskipTests compile -pl backend-api -am` on JDK 21 fails; on JDK 26 succeeds.
- Cloud: AGENT MANUAL draft build with install/start; expect `[INSTALL] Exit code: 0` and `notaire cloud install complete`.
- Optional: assert install.sh contains Temurin 26 markers in a small workspace static test (not required for Gate 1).

## Regression Strategy

No backend/frontend code change. Preflight / `mvn verify` remain the product gates for main.

## Playwright Strategy

No UI change.

## Deployment Strategy

Merge PR; next Cloud RECURRING build uses tip `install.sh`. Human may still Save a proposed draft environment config (`install=bash .cursor/install.sh`, `start=bash .cursor/start.sh`).

## Rollback Strategy

Revert the PR; Cloud installs fall back to base-image JDK (currently 21) and tip builds fail again until re-fixed.

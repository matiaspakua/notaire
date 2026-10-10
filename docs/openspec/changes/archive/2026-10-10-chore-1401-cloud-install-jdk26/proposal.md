# Cloud install: Temurin JDK 26 for Cursor Agent builds

> Governed by [CONSTITUTION.md](../../../../CONSTITUTION.md). This proposal documents
> **only this change**; permanent documentation remains the single source of truth.

| Field | Value |
|-------|-------|
| GitHub Issue | #1401 |
| Use Case | CU76 – Quality Assurance and Testing Infrastructure |
| Branch | `cursor/cloud-install-jdk26-8fb3` |
| Gate 1 status | draft |

## Objetivo

Cloud Agent recurring environment builds fail on tip of `main` with
`release version 26 not supported` because the repo `java.version` is **26**
(CI uses Temurin 26) while `.cursor/install.sh` never installs JDK 26.
Agents boot from the last SUCCEEDED snapshot (2026-10-05) while tip installs keep failing.

## What Changes

- `.cursor/install.sh` idempotently installs Temurin JDK 26 and sets `JAVA_HOME` / `/usr/local/bin/java|javac` before Maven runs.
- OpenSpec change artifacts for #1401 (this folder).
- No application, API, schema, or CI workflow change (CI already pins Temurin 26).

## Reglas de negocio

| Rule | Source | New / Changed / Made explicit |
|------|--------|-------------------------------|
| Cloud Agent install must match the repo Java release used by CI | CU76 / `.github/workflows/ci.yml` `JAVA_VERSION` | Made explicit |

## Capabilities

### New Capabilities

- (none — `skip_specs: true`)

### Modified Capabilities

- (none)

## Impact Analysis

### Módulos afectados

| Module | Touched | What changes |
|--------|---------|--------------|
| `backend-api` | no | — |
| `frontend` | no | — |
| `frontend-swing` | no | — |
| `infra` / observability | no | — |
| CI/CD (`.github/workflows`) | no | — |
| Cloud bootstrap (`.cursor/`) | yes | JDK 26 in `install.sh` |

### Surface area

- Entities / Endpoints / Flyway / Configuration / Dependencies: none
- **BREAKING** for API clients: no

### Architecture review

Tooling/bootstrap only. No ADR.

## Documentation Impact

| Permanent document | What must change |
|--------------------|------------------|
| `CHANGELOG.md` | one entry for Cloud install JDK 26 |
| `docs/300-development/304-ai-sdlc-cloud/` (if it still says Java 21 for agents) | note JDK 26 requirement if present |

## Out of Scope

- Changing `java.version` in the Maven POM (already 26).
- Dashboard Save of proposed environment builds (human action; see env-save workflow).
- Product feature work.

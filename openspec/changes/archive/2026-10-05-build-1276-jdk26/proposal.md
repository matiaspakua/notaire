# Move the toolchain to JDK 26

> Governed by [CONSTITUTION.md](../../../CONSTITUTION.md). This proposal documents
> **only this change**; permanent documentation remains the single source of truth.

| Field | Value |
|-------|-------|
| GitHub Issue | #1276 |
| Use Case | CU76 – Quality Assurance and Testing Infrastructure |
| Branch | `build/1276_jdk26` |
| Gate 1 status | draft |

## Objetivo

Dependabot moved the backend builder to the floating `maven:3-eclipse-temurin-26-alpine`, which fails the image-pin guard (#1045) on `main` and blocks every push, while the runtime sits on JDK 24 and CI on 21. Make the whole toolchain JDK 26 and pin the images to a minor.

## What Changes

- Root `pom.xml` `java.version` becomes 26.
- Every workflow `setup-java` and the `JAVA_VERSION` variables become 26.
- `backend-api/Dockerfile` and `Dockerfile.slim` use `maven:3.10.0-eclipse-temurin-26-alpine` and `eclipse-temurin:26.0.2.1_1-jre-alpine`.
- A guard test asserts every toolchain reference is 26.
- ADR-017, the devsecops guide and CLAUDE.md state JDK 26.

## Reglas de negocio

| Rule | Source | New / Changed / Made explicit |
|------|--------|-------------------------------|
| Container bases are pinned to a minor or digest | #1045 | Made explicit |
| One JDK version across build, CI and runtime | #1276 | New |

## Capabilities

### New Capabilities

- `jdk-toolchain`: A single JDK version (26) for build, CI and runtime.

### Modified Capabilities

- (none)

## Impact Analysis

### Módulos afectados

| Module | Touched | What changes |
|--------|---------|--------------|
| `backend-api` | yes | Dockerfiles; compiled with `--release 26` |
| `frontend` | no | — |
| Docs / scripts / CI | yes | workflows, guard test, ADR-017, devsecops guide, CLAUDE.md |

### Surface area

- Endpoints: none; Flyway: none; configuration: none
- Dependencies: plugins and libraries must support class files of Java 26 (verified by the full build)
- Risk: JDK 26 is not an LTS release; Spring Boot or a plugin could reject it. Mitigation: the full `mvn verify` and the image smoke test run on 26 before the PR; rollback is a revert.

### Architecture review

Updates the base-image decision of ADR-017; no new ADR.

## Documentation Impact

| Permanent document | What must change |
|--------------------|------------------|
| `docs/200-architecture/202-ADR/ADR-017-container-base-images.md` | JDK 26 and current pins |
| `docs/200-architecture/208-devsecops/README.md` | `JAVA_VERSION` |
| `CLAUDE.md` | Java version |
| `CHANGELOG.md` | one entry |

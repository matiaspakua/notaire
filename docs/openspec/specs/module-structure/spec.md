# module-structure Specification

## Purpose

Keep the Maven reactor and the build tooling free of a module with no second consumer, and make the REST API the only contract external services use. Source: #1255; owner CU76.

## Requirements

### Requirement: backend-api owns its DTOs

The system MUST compile every class of the former module (`com.licensis.notaire.dto`, `dto.exceptions`, `dto.interfaces` and `jpa.exceptions.PreexistingEntityException`) as part of `backend-api`, under their existing packages, and MUST NOT change any JSON shape exposed by the REST API.

#### Scenario: DTOs compile from backend-api

- **WHEN** a DTO class is loaded in a backend test
- **THEN** its code source is the `backend-api` build output and not a dependency jar

#### Scenario: JSON contract is unchanged

- **WHEN** the OpenAPI document is exported before and after the change
- **THEN** both documents are identical and the integration and Bruno suites pass

### Requirement: The reactor has a single module

The system MUST list only `backend-api` as a module of the root `pom.xml`, MUST NOT declare a dependency on `notaire-shared` in any live manifest, and MUST build the backend image without `notaire-shared` sources.

#### Scenario: Root reactor has one module

- **WHEN** the root `pom.xml` is read
- **THEN** its modules are exactly `backend-api`

#### Scenario: Backend does not depend on the module

- **WHEN** `backend-api/pom.xml` is read
- **THEN** no dependency has the artifact `notaire-shared`

#### Scenario: Docker build needs no shared sources

- **WHEN** `backend-api/Dockerfile` and `.dockerignore` are read
- **THEN** neither names `notaire-shared`, and the image still builds

#### Scenario: No live manifest or tooling references the module

- **WHEN** CODEOWNERS, release-please, CI workflows, `scripts/`, `testing/scripts/`, `.cursor/` and `.aisdlc/` are searched
- **THEN** none references `notaire-shared`

### Requirement: The retired module is removed from the tree

The system MUST NOT contain the former module (its history stays in git, tag `archive-monorepo-pre-split`), and MUST fail a guard test if the folder or a `deprecated/` folder reappears.

#### Scenario: Module folder is gone

- **WHEN** the repository tree is listed
- **THEN** neither `notaire-shared/` nor `deprecated/` exists

### Requirement: Code that only served the module is removed

The system MUST NOT contain `SharedModuleMetrics` or the `notaire_shared_version` gauge, because nothing uses them.

#### Scenario: Dead module observers are gone

- **WHEN** the backend sources and tests are searched for `SharedModuleMetrics` and `notaire_shared`
- **THEN** there are no matches

### Requirement: External services use the REST API

The documentation MUST state that external services and clients consume `/api/v1` through the OpenAPI contract and MUST NOT describe a shared Java DTO library.

#### Scenario: External services use the API

- **WHEN** the setup guide, DTO guide, README and ADR-025 are read
- **THEN** they point to the OpenAPI contract and none lists `notaire-shared` as a live module

# Deprecate notaire-shared; backend-api owns its DTOs

> Governed by [CONSTITUTION.md](../../../CONSTITUTION.md). This proposal documents
> **only this change**; permanent documentation remains the single source of truth.

| Field | Value |
|-------|-------|
| GitHub Issue | #1255 |
| Use Case | CU76 – Quality Assurance and Testing Infrastructure (build and module structure; same Use Case as #581) |
| Branch | `refactor/1255_deprecate_notaire_shared` |
| Gate 1 status | draft |

## Objetivo

`notaire-shared` existed so the Swing client and the backend could share DTO classes. Swing is gone (#1046): `backend-api` is the only Java consumer (66 main and 35 test files import the DTOs), and `frontend/` consumes the REST API as JSON. The module is a second Maven module, a Docker `COPY` layer and a `-am` build flag with no second consumer. Retire it: `backend-api` owns its DTOs, any external service consumes the REST API, and the module folder moves to `deprecated/`.

## What Changes

- Every class of the module (56 files: the DTOs plus `GenericDto`, `DtoValido`, `TypeItem`, `DtoInvalidoException`, `PreexistingEntityException`) moves with `git mv` into `backend-api`, keeping its package, so no `import` changes. All of them are used: the DTOs extend `GenericDto` or implement `DtoValido`.
- Dead code that only existed to observe the module is deleted: `SharedModuleMetrics`, the `notaire_shared_version` gauge and their tests (no caller records into them; no dashboard reads them).
- `backend-api` drops the `notaire-shared` dependency; the root `pom.xml` lists only `backend-api`.
- Dockerfile, `.dockerignore`, CODEOWNERS, `release-please-config.json`, `.cursor/install.sh`, `.aisdlc/project.yml`, `scripts/check-tdd-evidence.sh`, `scripts/check-agent-rules.sh` and `testing/scripts/generate-coverage-report.sh` stop referencing the module.
- The remaining folder moves to `deprecated/notaire-shared/`; its `pom.xml` is stored as `pom.xml.archived`, like `frontend-swing`.
- A guard test fails if a live manifest references the module or the folder reappears at the repo root.
- ADR-024 records the decision and supersedes the three-module structure of ADR-002.

## Reglas de negocio

| Rule | Source | New / Changed / Made explicit |
|------|--------|-------------------------------|
| External services consume the REST API (`/api/v1`, OpenAPI), never Java DTO classes | CONSTITUTION §4, #1255 | Made explicit |
| The REST contract and JSON shapes do not change | #1255 | Made explicit |

## Capabilities

### New Capabilities

- `module-structure`: The Maven reactor has one module; `backend-api` owns its DTOs; retired modules live under `deprecated/`.

### Modified Capabilities

- (none)

## Impact Analysis

### Módulos afectados

| Module | Touched | What changes |
|--------|---------|--------------|
| `backend-api` | yes | receives the DTO sources, drops the dependency, loses the dead metrics code, Dockerfile |
| `frontend` | no | already consumes the API only |
| `notaire-shared` | yes | retired: emptied and moved to `deprecated/notaire-shared/` |
| Docs / scripts / CI | yes | build and tooling references, ADR-024, SAD, setup and DTO guides |

### Surface area

- Endpoints: none; JSON shapes unchanged (verified by an OpenAPI export diff)
- Entities / Flyway / Configuration: none
- Dependencies: removes the internal `notaire-shared` artifact; `jackson-annotations` is already on the backend classpath through Spring Boot
- Metrics: `notaire_shared_*` series disappear (never recorded, no dashboard or alert uses them)
- Risks: a stale `target/` or local Maven repository still holds `notaire-shared-1.0-SNAPSHOT.jar`; a clean build removes the ambiguity. Rename detection in Git is lost for files that Spotless has to reformat, so the move and the formatting are separate commits.

### Architecture review

Structural: supersedes the three-module decision of ADR-002 and the module list of ADR-005. Recorded in ADR-024. The `repository`/`service`/`adapter` layering is untouched. Follow-up outside this change: CONSTITUTION §5 step 4 still lists `notaire-shared`; amending the Constitution needs its own PR (§12).

## Documentation Impact

| Permanent document | What must change |
|--------------------|------------------|
| `docs/200-architecture/202-ADR/ADR-024-retire-notaire-shared.md` | new ADR; index in `ADR/README.md` |
| `docs/200-architecture/202-ADR/ADR-002-module-structure.md`, `ADR-017-container-base-images.md` | status note / Docker build context |
| `docs/200-architecture/201-SAD/sad.md`, `204-diagrams/*.puml` | module view and diagrams |
| `docs/200-architecture/208-devsecops/README.md`, `docs/300-development/301-setup/README.md`, `302-code-standards/DTO-MAPPING-GUIDE.md`, `RELEASE.md`, `DEVELOPMENT-PLAN.md` | build commands, DTO location |
| `README.md`, `backend-api/README.md`, `CLAUDE.md`, `.claude/rules/refactoring.md`, `.claude/agents/backend-implementer.md`, `.claude/skills/maven-build/SKILL.md`, `.agents/skills/maven-build/SKILL.md`, `local-ai/sdlc/WORKER.md`, `openspec/config.yaml`, `openspec/schemas/notaire-sdlc/schema.yaml` | module list and build commands |
| `deprecated/README.md` | new row for `notaire-shared/` |
| `CHANGELOG.md` | one entry |

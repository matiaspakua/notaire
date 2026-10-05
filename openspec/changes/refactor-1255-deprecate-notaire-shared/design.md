> Governed by [CONSTITUTION.md](../../../CONSTITUTION.md) — §6 Quality Gates, §7 Testing Rules, §11 Release Rules.

## Context

Issue #1255, Use Case CU76. `notaire-shared` holds 56 DTO classes, `GenericDto`, `TypeItem`, `DtoInvalidoException`, `DtoValido` and `PreexistingEntityException` (in a `jpa.exceptions` package that `backend-api` also has). Its only dependency is `jackson-annotations`. Nothing in `frontend/` references it, and no class name collides with one in `backend-api`.

## Goals / Non-Goals

**Goals:** One Maven module; `backend-api` owns the DTO sources; no live reference to the module; the old folder under `deprecated/`; the REST contract byte-for-byte equal.
**Non-Goals:** Splitting DTOs into a different package, renaming them to `DtoEntityName` where they differ, or fixing controllers that return entities (#577); generating a client library (#1197 decides the multi-repo shape).

## Decisions

1. Move the sources into `backend-api` under the same package (`com.licensis.notaire.dto`), using `git mv` so history follows. Rejected: a new package (rewrites 100 imports for no gain).
2. Delete `GenericDto`, `DtoValido`, `SharedModuleMetrics` and the gauge instead of moving them: nothing uses them, and the metrics observe a module that no longer exists. Rejected: keeping them under a new name (dead code, rule 13).
3. Keep the root `pom.xml` as the parent with a single module, because `backend-api` inherits its parent and the build commands keep working. Rejected: flattening `backend-api` into the root (larger move, unrelated).
4. Archive the manifest as `pom.xml.archived`, the precedent of `frontend-swing`, so Dependabot and Maven never see a live manifest. Rejected: leaving a live `pom.xml` under `deprecated/`.
5. Enforce with a Python guard test next to the other repo-structure guards (`scripts/test_*.py` with a `scripts/tests` wrapper) plus one JUnit test for class ownership. Rejected: ArchUnit (new dependency for one assertion).
6. Format the moved files in a separate commit after the pure `git mv`, because Spotless ratchets from `origin/main` and treats moved files as changed.
7. Do not amend the Constitution here (§12 requires a dedicated PR); record the stale module list in §5 step 4 as a follow-up issue.

## Riesgos / Trade-offs

- A stale `notaire-shared-1.0-SNAPSHOT.jar` in `~/.m2` could mask a missing class locally; mitigated by a clean build in CI and the Docker build, and by `DtoOwnershipTest`.
- `release-please` loses one tracked manifest; the root version stays driven by the remaining entries.
- Other branches that edit `notaire-shared/` files will conflict on rebase; the rename is detected by Git for unformatted moves.

## Testing Strategy

| Scenario (spec) | Test level | Test class / file |
|-----------------|------------|-------------------|
| DTOs compile from backend-api | unit | `DtoOwnershipTest` |
| JSON contract is unchanged | integration + API | `scripts/export-openapi.sh` diff, `mvn verify -pl backend-api`, Bruno via `bash testing/scripts/test.sh` |
| Root reactor has one module | unit (repo guard) | `scripts/test_notaire_shared_retired.py` |
| Backend does not depend on the module | unit (repo guard) | `scripts/test_notaire_shared_retired.py` |
| Docker build needs no shared sources | unit (repo guard) + build | `scripts/test_notaire_shared_retired.py`, `docker build` in `preflight.sh --full` |
| No live manifest or tooling references the module | unit (repo guard) | `scripts/test_notaire_shared_retired.py` |
| Module folder is archived | unit (repo guard) | `scripts/test_notaire_shared_retired.py` |
| Archived manifest is not live | unit (repo guard) | `scripts/test_notaire_shared_retired.py` |
| Dead module observers are gone | unit + guard | `ObservabilityTest` (block removed), `scripts/test_notaire_shared_retired.py` |
| External services use the API | unit (repo guard) | `scripts/test_notaire_shared_retired.py` |

- New unit tests (`src/test/java/.../unit/`): `DtoOwnershipTest`
- New repo guard: `scripts/test_notaire_shared_retired.py` with wrapper `scripts/tests/test_notaire_shared_retired.py`
- Updated tests: `scripts/test_repo_hygiene.py` (CODEOWNERS covers live modules), `ObservabilityTest`
- Coverage impact: neutral to positive; removed code was only covered by its own test

## Regression Strategy

- Existing tests affected: `ObservabilityTest` (the `SharedModuleMetrics` block is deleted with the class), `test_repo_hygiene.py`
- Full suite command: `mvn verify -pl backend-api; python3 -m unittest discover -s scripts/tests; bash scripts/preflight.sh`
- HTTP/Bruno API suite: unchanged, must stay green
- Legacy paths at risk: Docker image build and `.cursor/install.sh` build line

## Playwright Strategy

No UI change. The Playwright suite runs unchanged in CI as regression evidence; no new spec is needed.

## Deployment Strategy

- Flyway migration required: no
- Deployment order / coupling: single PR; the image build no longer copies `notaire-shared`
- Configuration or `.env` keys: none
- Feature flag: no
- Smoke test after deploy (Gate 5): CI and `cd.yml` green on the merge commit; `/actuator/health` is UP and `GET /v3/api-docs` matches the pre-change export

## Rollback Strategy

- Revert the PR; the folder, the dependency and the Dockerfile lines return together.

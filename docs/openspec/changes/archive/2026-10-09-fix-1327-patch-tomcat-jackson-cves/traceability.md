# Traceability

> Governed by [CONSTITUTION.md](../../../../CONSTITUTION.md) — P4 Traceability.

## Chain

```text
Issue → Specification → Tasks → Commits → PR → Merge → Release
```

| Link | Reference | Status |
|------|-----------|--------|
| Issue | #1327 | open → in progress |
| Use Case | CU78 – Seguridad, Privacidad y Cumplimiento | exists |
| Specification | `docs/openspec/changes/fix-1327-patch-tomcat-jackson-cves/` | Gate 1 draft |
| Branch | `fix/1327_patch_tomcat_jackson_cves` | created from updated `main` |
| Tasks | `tasks.md` | in progress |
| Commits | see branch | local, not pushed |
| Pull Request | — | pending (Owner approval) |
| CI run | — | pending |
| Merge commit | — | pending |
| Release / tag | — | pending |
| Smoke test | — | pending |

## Requirement coverage

| Scenario (Acceptance Criterion) | Test | Status |
|---------------------------------|------|--------|
| Patched Tomcat | `backend-api/src/test/java/com/licensis/notaire/security/PatchedDependencyFloorTest.java` | passing |
| Patched Jackson | `backend-api/src/test/java/com/licensis/notaire/security/PatchedDependencyFloorTest.java` | passing |
| No commons-collections gadget | `backend-api/src/test/java/com/licensis/notaire/security/PatchedDependencyFloorTest.java` | passing |

## Permanent documentation updated

| Document | Updated | Commit |
|----------|---------|--------|
| `CHANGELOG.md` | yes | branch commit |
| `pom.xml` | yes | branch commit |

## Gate log

| Gate | Condition | Passed | Evidence |
|------|-----------|--------|----------|
| 1 | Issue + Specification + Acceptance Criteria | yes | `bash workspace/sdlc/validate-sdlc-plan.sh fix-1327-patch-tomcat-jackson-cves` |
| 2 | Failing tests written, test cases designed | yes | `PatchedDependencyFloorTest` observed failing on main (3 of 4 cases) |
| 3 | Suite green, coverage held, docs updated | partial | backend suite and Bruno green; Trivy 0 HIGH/CRITICAL for backend-api; oasdiff: specs identical; run_pipeline.sh not run (no Docker) |
| 4 | CI green, review approved, no conflicts | pending | — |
| 5 | Deployed, smoke test passed, Issue closed | pending | — |

## Exceptions

`workspace/sdlc/run_pipeline.sh` and the Testcontainers `pg-integration` tests need Docker, unavailable on the agent box.

# Traceability

> Governed by [CONSTITUTION.md](../../../CONSTITUTION.md) — P4 Traceability.

## Chain

```text
Issue → Specification → Tasks → Commits → PR → Merge → Release
```

| Link | Reference | Status |
|------|-----------|--------|
| Issue | #1255 | open → in progress |
| Use Case | CU76 – Quality Assurance and Testing Infrastructure | exists |
| Related | #581 (superseded: the JPA-layer leak disappears with the module), #1197 (multi-repo split), #1046 (Swing removal) | referenced |
| Specification | `openspec/changes/refactor-1255-deprecate-notaire-shared/` | Gate 1 draft |
| Branch | `refactor/1255_deprecate_notaire_shared` | created from updated `main` |
| Tasks | `tasks.md` | in progress |
| Commits | see branch | in progress |
| Pull Request | — | pending |
| CI run | — | pending |
| Merge commit | — | pending |
| Release / tag | — | pending |
| Smoke test | — | pending |

## Requirement coverage

| Scenario (Acceptance Criterion) | Test | Status |
|---------------------------------|------|--------|
| DTOs compile from backend-api | `DtoOwnershipTest` | pending |
| JSON contract is unchanged | OpenAPI export diff + Bruno + `mvn verify` | pending |
| Root reactor has one module | `test_notaire_shared_retired.py` | pending |
| Backend does not depend on the module | `test_notaire_shared_retired.py` | pending |
| Docker build needs no shared sources | `test_notaire_shared_retired.py` + Docker build in preflight | pending |
| No live manifest or tooling references the module | `test_notaire_shared_retired.py` | pending |
| Module folder is archived | `test_notaire_shared_retired.py` | pending |
| Archived manifest is not live | `test_notaire_shared_retired.py` | pending |
| Dead module observers are gone | `ObservabilityTest` (block removed), `test_notaire_shared_retired.py` | pending |
| External services use the API | `test_notaire_shared_retired.py` (docs guard) | pending |

## Permanent documentation updated

| Document | Updated | Commit |
|----------|---------|--------|
| `docs/200-architecture/202-ADR/ADR-024-retire-notaire-shared.md` | pending | — |
| `docs/200-architecture/202-ADR/ADR-002-module-structure.md` | pending | — |
| `docs/200-architecture/201-SAD/sad.md` | pending | — |
| `docs/300-development/302-code-standards/DTO-MAPPING-GUIDE.md` | pending | — |
| `deprecated/README.md` | pending | — |
| `CHANGELOG.md` | pending | — |

## Gate log

| Gate | Condition | Passed | Evidence |
|------|-----------|--------|----------|
| 1 | Issue + Specification + Acceptance Criteria | pending | `bash scripts/validate-sdlc-plan.sh` |
| 2 | Failing tests written, test cases designed | pending | — |
| 3 | Suite green, coverage held, docs updated | pending | — |
| 4 | CI green, review approved, no conflicts | pending | — |
| 5 | Deployed, smoke test passed, Issue closed | pending | — |

## Exceptions

None.

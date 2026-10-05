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
| Related | #1263 (Constitution follow-up), #581 (superseded: the JPA-layer leak disappears with the module), #1197 (multi-repo split), #1046 (Swing removal) | referenced |
| Specification | `openspec/changes/refactor-1255-deprecate-notaire-shared/` | Gate 1 validated |
| Branch | `refactor/1255_deprecate_notaire_shared` | created from updated `main` |
| Tasks | `tasks.md` | implemented; PR steps pending |
| Commits | see branch | done |
| Pull Request | — | pending |
| CI run | — | pending |
| Merge commit | — | pending |
| Release / tag | — | pending |
| Smoke test | — | pending |

## Requirement coverage

| Scenario (Acceptance Criterion) | Test | Status |
|---------------------------------|------|--------|
| DTOs compile from backend-api | `DtoOwnershipTest` | passing |
| JSON contract is unchanged | OpenAPI export diff + Bruno + `mvn verify` | passing |
| Root reactor has one module | `test_notaire_shared_retired.py` | passing |
| Backend does not depend on the module | `test_notaire_shared_retired.py` | passing |
| Docker build needs no shared sources | `test_notaire_shared_retired.py` + Docker build in preflight | passing |
| No live manifest or tooling references the module | `test_notaire_shared_retired.py` | passing |
| Module folder is archived | `test_notaire_shared_retired.py` | passing |
| Archived manifest is not live | `test_notaire_shared_retired.py` | passing |
| Dead module observers are gone | `ObservabilityTest` (block removed), `test_notaire_shared_retired.py` | passing |
| External services use the API | `test_notaire_shared_retired.py` (docs guard) | passing |

## Permanent documentation updated

| Document | Updated | Commit |
|----------|---------|--------|
| `docs/200-architecture/202-ADR/ADR-025-retire-notaire-shared.md` | done | `8c9cfc9f` |
| `docs/200-architecture/202-ADR/ADR-002-module-structure.md` | done | `8c9cfc9f` |
| `docs/200-architecture/201-SAD/sad.md` | done | `8c9cfc9f` |
| `docs/300-development/302-code-standards/DTO-MAPPING-GUIDE.md` | done | `8c9cfc9f` |
| `deprecated/README.md` | done | `8c9cfc9f` |
| `CHANGELOG.md` | done | `8c9cfc9f` |

## Gate log

| Gate | Condition | Passed | Evidence |
|------|-----------|--------|----------|
| 1 | Issue + Specification + Acceptance Criteria | yes | `bash scripts/validate-sdlc-plan.sh` |
| 2 | Failing tests written, test cases designed | yes | `35f336fc` |
| 3 | Suite green, coverage held, docs updated | pending | — |
| 4 | CI green, review approved, no conflicts | pending | — |
| 5 | Deployed, smoke test passed, Issue closed | pending | — |

## Exceptions

None.

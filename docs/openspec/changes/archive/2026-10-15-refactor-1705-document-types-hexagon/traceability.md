# Traceability

> Governed by [CONSTITUTION.md](../../../../CONSTITUTION.md) — P4 Traceability.

## Chain

```text
Issue → Specification → Tasks → Commits → PR → Merge → Release
```

| Link | Reference | Status |
|------|-----------|--------|
| Issue | #1705 | in-progress (label write 403 — fleet token) |
| Use Case | CU27 / CU32 / CU38 / CU65 (document types); CU76 characterization | exists |
| Specification | `docs/openspec/changes/refactor-1705-document-types-hexagon/` | Gate 1 |
| Branch | `refactor/1705_document_types_hexagon` | created |
| Tasks | `tasks.md` | pending |
| Commits | pending | pending |
| Pull Request | pending | pending |
| CI run | pending | pending |
| Merge commit | pending | pending |
| Release / tag | pending | pending |
| Smoke test | pending | pending |

## Requirement coverage

| Scenario (Acceptance Criterion) | Test | Status |
|---------------------------------|------|--------|
| read search and in-use (both tables) | DocumentTypeApiCharacterizationTest | pending |
| locked when in use (edit/delete 409 bodies) | DocumentTypeApiCharacterizationTest | pending |
| stale version | DocumentTypeApiCharacterizationTest | pending |
| use cases without Spring | DocumentTypeUseCasesTest | pending |
| architecture baseline shrinks | HexagonArchitectureTest | pending |

## Permanent documentation updated

| Document | Updated | Commit |
|----------|---------|--------|
| `docs/200-architecture/202-ADR/ADR-030-hexagonal-architecture-full-adoption.md` | no | pending |

## Gate log

| Gate | Condition | Passed | Evidence |
|------|-----------|--------|----------|
| 1 | Issue + Specification + Acceptance Criteria | yes | #1705, this folder |
| 2 | Failing tests written, test cases designed | pending | pending |
| 3 | Suite green, coverage held, docs updated | pending | pending |
| 4 | CI green, review approved, no conflicts | pending | pending |
| 5 | Deployed, smoke test passed, Issue closed | pending | pending |

## Exceptions

Issue label/comment writes return 403 for the fleet integration token; board coherence (#1551) requires human paste.

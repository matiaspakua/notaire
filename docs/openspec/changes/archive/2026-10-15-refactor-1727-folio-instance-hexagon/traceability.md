# Traceability

> Governed by [CONSTITUTION.md](../../../../CONSTITUTION.md) — P4 Traceability.
> This is the change's ledger. It is created during planning with the upstream
> links filled in, and completed as the change moves through the gates. Rows below
> Tasks stay `pending` until the corresponding step actually happens — never
> pre-fill them.

## Chain

```
Issue → Specification → Tasks → Commits → PR → Merge → Release
```

| Link | Reference | Status |
|------|-----------|--------|
| Issue | #1727 | in-progress |
| Use Case | Architectural refactor under epic #1693 (ADR-030); folios CU28/CU33/CU63/CU06; CU76 characterization | exists |
| Specification | `docs/openspec/changes/refactor-1727-folio-instance-hexagon/` | passed |
| Branch | `cursor/1727-folio-instance-hexagon-a603` | created |
| Tasks | `tasks.md` | in progress |
| Commits | pending | pending |
| Pull Request | pending | pending |
| CI run | pending | pending |
| Merge commit | pending | pending |
| Release / tag | pending | pending |
| Smoke test | pending | pending |

## Requirement coverage

| Scenario (Acceptance Criterion) | Test | Status |
|---------------------------------|------|--------|
| CRUD and search keep the wire contract | FolioControllerTest | passing |
| Utilizado cannot be modified or deleted except same-deed re-save | FolioUseCasesTest / FolioControllerTest | passing |
| linking a deed forces Utilizado | FolioUseCasesTest | passing |
| in-use endpoint reports Utilizado | FolioUseCasesTest / FolioControllerTest | passing |
| controller has no persistence imports | HexagonArchitectureTest (FolioController R1 cleared) | passing |

## Permanent documentation updated

| Document | Updated | Commit |
|----------|---------|--------|
| `docs/200-architecture/202-ADR/ADR-030-hexagonal-architecture-full-adoption.md` | yes | pending |
| `CHANGELOG.md` | yes — Unreleased after #1718 merged | pending |

## Gate log

| Gate | Condition | Passed | Evidence |
|------|-----------|--------|----------|
| 1 | Issue + Specification + Acceptance Criteria | yes | #1727, this folder |
| 2 | Failing tests written, test cases designed | yes | FolioUseCasesTest |
| 3 | Suite green, coverage held, docs updated | pending | Folio* + ArchUnit green locally |
| 4 | CI green, review approved, no conflicts | pending | pending |
| 5 | Deployed, smoke test passed, Issue closed | pending | pending |

## Exceptions

CHANGELOG Unreleased omitted while #1718 (release-please) remains OPEN.

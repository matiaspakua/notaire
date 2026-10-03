# Traceability

> Governed by [CONSTITUTION.md](../../../CONSTITUTION.md) — P4 Traceability.
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
| Issue | #801 | open (in-progress label ACL denied for bot) |
| Use Case | CU72 – Gestión de Documentos Presentados | exists (to update) |
| Specification | `openspec/changes/fix-801-submitted-document-mapping/` | Gate 1 complete |
| Branch | `cursor/fix-801-submitted-document-mapping-69d3` | active |
| Tasks | `tasks.md` | Gate 1 done; implementation pending |
| Commits | — | pending |
| Pull Request | — | pending |
| CI run | pending (coordinator: `bash scripts/check-heavy-ci.sh <pr>`) | pending |
| Merge commit | — | pending |
| Release / tag | — | pending |
| Smoke test | — | pending |

## Requirement coverage

| Scenario (Acceptance Criterion) | Test | Status |
|---------------------------------|------|--------|
| DocumentType association is mapped on SubmittedDocument | `SubmittedDocumentEntityTest#shouldMapDocumentTypeAssociation` | pending |
| DocumentType inverse mappedBy matches owning property | `SubmittedDocumentEntityTest#shouldDeclareMappedByDocumentTypeOnDocumentType` | pending |
| getDto with null procedure does not NPE | `SubmittedDocumentEntityTest#shouldNotNpeWhenGetDtoWithNullProcedure` | pending |
| getDto with procedure present includes procedure DTO | `SubmittedDocumentEntityTest#shouldIncludeProcedureDtoWhenPresent` | pending |

## Permanent documentation updated

| Document | Updated | Commit |
|----------|---------|--------|
| `docs/100-business/102-use-cases/CU72 – Gestión de Documentos Presentados.md` | no | — |
| `CHANGELOG.md` | no | — |

## Gate log

| Gate | Condition | Passed | Evidence |
|------|-----------|--------|----------|
| 1 | Issue + Specification + Acceptance Criteria | yes | `bash scripts/validate-sdlc-plan.sh fix-801-submitted-document-mapping` |
| 2 | Failing tests written, test cases designed | pending | — |
| 3 | Suite green, coverage held, docs updated | pending | — |
| 4 | CI green, review approved, no conflicts | pending | — |
| 5 | Deployed, smoke test passed, Issue closed | pending | — |

## Exceptions

None. `in-progress` label could not be applied (GitHub GraphQL: Resource not accessible by integration).

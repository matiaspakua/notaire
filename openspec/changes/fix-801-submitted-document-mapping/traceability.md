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
| Use Case | CU72 – Gestión de Documentos Presentados | updated |
| Specification | `openspec/changes/fix-801-submitted-document-mapping/` | Gate 1 complete |
| Branch | `cursor/fix-801-submitted-document-mapping-69d3` | active |
| Tasks | `tasks.md` | implementation + docs done; merge pending |
| Commits | `18f43840` openspec; `5d9cb61b` tests; `be3fc61d` fix; `9f43c37c` docs; `9de27283` status | recorded |
| Pull Request | https://github.com/matiaspakua/notaire/pull/1195 | draft |
| CI run | pending (coordinator: `bash scripts/check-heavy-ci.sh 1195`) | pending |
| Merge commit | — | pending |
| Release / tag | — | pending |
| Smoke test | — | pending |

## Requirement coverage

| Scenario (Acceptance Criterion) | Test | Status |
|---------------------------------|------|--------|
| DocumentType association is mapped on SubmittedDocument | `SubmittedDocumentEntityTest#shouldMapDocumentTypeAssociation` | passing |
| DocumentType inverse mappedBy matches owning property | `SubmittedDocumentEntityTest#shouldDeclareMappedByDocumentTypeOnDocumentType` | passing |
| getDto with null procedure does not NPE | `SubmittedDocumentEntityTest#shouldNotNpeWhenGetDtoWithNullProcedure` | passing |
| getDto with procedure present includes procedure DTO | `SubmittedDocumentEntityTest#shouldIncludeProcedureDtoWhenPresent` | passing |

## Permanent documentation updated

| Document | Updated | Commit |
|----------|---------|--------|
| `docs/100-business/102-use-cases/CU72 – Gestión de Documentos Presentados.md` | yes | `9f43c37c` |
| `CHANGELOG.md` | yes | `9f43c37c` |

## Gate log

| Gate | Condition | Passed | Evidence |
|------|-----------|--------|----------|
| 1 | Issue + Specification + Acceptance Criteria | yes | `bash scripts/validate-sdlc-plan.sh fix-801-submitted-document-mapping` |
| 2 | Failing tests written, test cases designed | yes | TDD red: missing association API compile failure; then getDto Boolean NPE before full guard |
| 3 | Suite green, coverage held, docs updated | yes | `mvn verify -pl backend-api -am` — 1955 tests, 0 failures; instr ~85.0% / branch ~73.5% |
| 4 | CI green, review approved, no conflicts | pending | draft PR #1195 |
| 5 | Deployed, smoke test passed, Issue closed | pending | — |

## Exceptions

None. `in-progress` label could not be applied (GitHub GraphQL: Resource not accessible by integration).

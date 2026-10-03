# Traceability

> Governed by [CONSTITUTION.md](../../../CONSTITUTION.md) — P4 Traceability.
> This is the change's ledger. It is created during planning with the upstream
> links filled in, and completed as the change moves through the gates. Rows below
> Tasks stay `pending` until the corresponding step actually happens — never
> pre-fill them.

## Chain

```text
Issue → Specification → Tasks → Commits → PR → Merge → Release
```

| Link | Reference | Status |
|------|-----------|--------|
| Issue | #800 | open (in-progress label ACL denied for bot) |
| Use Case | CU27, CU32, CU04, CU72 | exists / to update CU27/CU32 |
| Related | #837 (expires/dueDays/deliveredBy + inheritance — shipped) | referenced |
| Specification | `openspec/changes/feat-800-tipo-documento-defaults/` | Gate 1 complete |
| Branch | `cursor/feat-800-tipo-documento-defaults-69d3` | active |
| Tasks | `tasks.md` | pending |
| Commits | — | pending |
| Pull Request | — | pending |
| CI run | pending (coordinator: `bash scripts/check-heavy-ci.sh <pr>`) | pending |
| Merge commit | — | pending |
| Release / tag | — | pending |
| Smoke test | — | pending |

## Requirement coverage

| Scenario (Acceptance Criterion) | Test | Status |
|---------------------------------|------|--------|
| Create form defaults enabled=true, returned=false | Vitest `tipo-documento-enabled-returned.test.ts` + Playwright CU27-GW04 | pending |
| Create persists enabled/returned from form | Playwright CU27-GW05 + `DocumentTypeReferentialIntegrityTest` | pending |
| Edit pre-fills and updates enabled/returned | Playwright CU32-GW02 + backend update IT | pending |
| API create honors enabled=false when provided | `DocumentTypeReferentialIntegrityTest#shouldPersistEnabledAndReturnedOnCreate` | pending |
| API GET/DTO includes returned | same IT / getById assertion | pending |
| SubmittedDocument inheritance (#837) still green | `SubmittedDocumentControllerTest` | pending (confirm) |

## Permanent documentation updated

| Document | Updated | Commit |
|----------|---------|--------|
| CU27 | pending | — |
| CU32 | pending | — |
| `CHANGELOG.md` | pending | — |

## Gate log

| Gate | Condition | Passed | Evidence |
|------|-----------|--------|----------|
| 1 | Issue + Specification + Acceptance Criteria | yes | `bash scripts/validate-sdlc-plan.sh feat-800-tipo-documento-defaults` |
| 2 | Failing tests written, test cases designed | pending | — |
| 3 | Suite green, coverage held, docs updated | pending | — |
| 4 | CI green, review approved, no conflicts | pending | — |
| 5 | Deployed, smoke test passed, Issue closed | pending | — |

## Exceptions

None. `in-progress` label could not be applied (GitHub GraphQL: Resource not accessible by integration).

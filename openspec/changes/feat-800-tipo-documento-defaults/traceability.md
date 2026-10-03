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
| Use Case | CU27, CU32, CU04, CU72 | exists / updated CU27/CU32 |
| Related | #837 (expires/dueDays/deliveredBy + inheritance — shipped) | referenced |
| Specification | `openspec/changes/feat-800-tipo-documento-defaults/` | Gate 1 complete |
| Branch | `cursor/feat-800-tipo-documento-defaults-69d3` | active |
| Tasks | `tasks.md` | implementation done; merge pending |
| Commits | `1cbf3113` `c2401bb2` `91572f28` `58267738` (+ follow-ups) | recorded |
| Pull Request | https://github.com/matiaspakua/notaire/pull/1205 | draft |
| CI run | pending (coordinator: `bash scripts/check-heavy-ci.sh 1205`) | pending |
| Merge commit | — | pending |
| Release / tag | — | pending |
| Smoke test | — | pending |

## Requirement coverage

| Scenario (Acceptance Criterion) | Test | Status |
|---------------------------------|------|--------|
| Create form defaults enabled=true, returned=false | Vitest + Playwright CU27-GW04 | passing |
| Create persists enabled/returned from form | Playwright CU27-GW05 + DocumentType IT | passing |
| Edit pre-fills and updates enabled/returned | Playwright CU32-GW02 + update IT | passing |
| API create honors enabled=false when provided | `shouldPersistEnabledAndReturnedOnCreate` | passing |
| API GET/DTO includes returned | same IT / getById | passing |
| SubmittedDocument inheritance (#837) still green | `SubmittedDocumentControllerTest` | passing |

## Permanent documentation updated

| Document | Updated | Commit |
|----------|---------|--------|
| CU27 | yes | `58267738` |
| CU32 | yes | `58267738` |
| `CHANGELOG.md` | yes | `58267738` |

## Gate log

| Gate | Condition | Passed | Evidence |
|------|-----------|--------|----------|
| 1 | Issue + Specification + Acceptance Criteria | yes | `bash scripts/validate-sdlc-plan.sh feat-800-tipo-documento-defaults` |
| 2 | Failing tests written, test cases designed | yes | Vitest red on checkboxes/i18n; IT red on returned/enabled |
| 3 | Suite green, coverage held, docs updated | yes | Playwright 7/7; DocumentType+SubmittedDocument ITs green |
| 4 | CI green, review approved, no conflicts | pending | draft PR #1205 |
| 5 | Deployed, smoke test passed, Issue closed | pending | — |

## Exceptions

None. `in-progress` label could not be applied (GitHub GraphQL: Resource not accessible by integration).

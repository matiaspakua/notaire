# Traceability

> Governed by [CONSTITUTION.md](../../../CONSTITUTION.md) — P4 Traceability.

## Chain

```text
Issue → Specification → Tasks → Commits → PR → Merge → Release
```

| Link | Reference | Status |
|------|-----------|--------|
| Issue | #1241 | open → in progress |
| Use Case | CU72 – Gestión de Documentos Presentados | exists |
| Related | — | referenced |
| Specification | `openspec/changes/fix-1241-submitted-document-partial-update/` | Gate 1 draft |
| Branch | `fix/1241_documento_presentado_partial_update` | created from updated `main` |
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
| Delivered flag update keeps the rest | `SubmittedDocumentPartialUpdateIntegrationTest` | pending |
| Name update keeps the flags | `SubmittedDocumentPartialUpdateIntegrationTest` | pending |
| Unknown document | `SubmittedDocumentPartialUpdateIntegrationTest` | pending |
| Date change recomputes the due date | `SubmittedDocumentPartialUpdateIntegrationTest` | pending |

## Permanent documentation updated

| Document | Updated | Commit |
|----------|---------|--------|
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

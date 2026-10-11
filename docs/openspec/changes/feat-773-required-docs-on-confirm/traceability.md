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
| Issue | #773 | open (remaining AC after #1240; apply in progress) |
| Use Case | CU02 – Iniciar Gestión (CU03, CU04, CU72) | exists |
| Related | #1240 MERGED (partial); parent #771 OPEN; #774 blocked until this closes | referenced |
| Specification | `docs/openspec/changes/feat-773-required-docs-on-confirm/` | Gate 1 validated |
| Branch | `cursor/feat-773-required-docs-on-confirm-69d3` | created from `ecb8cbfc` |
| Tasks | `tasks.md` | apply in progress |
| Commits | — | pending |
| Pull Request | — | pending |
| CI run | — | pending |
| Merge commit | — | pending |
| Release / tag | — | pending |
| Smoke test | — | pending |

## Requirement coverage

| Scenario (Acceptance Criterion) | Test | Status |
|---------------------------------|------|--------|
| Complete-case seeds pending required documents from procedure template | unit + integration | pending |
| Case summary lists pending required documents after confirm | unit + integration + Bruno | pending |
| Idempotent seed skips existing document types for the procedure | unit + integration | pending |
| PUT complete-case on existing procedure does not wipe registered docs | integration | pending |
| Empty template yields no seeded documents | unit | pending |
| E2E: checklist visible after complete-case | Playwright | pending |

## Permanent documentation updated

| Document | Updated | Commit |
|----------|---------|--------|
| `docs/100-business/102-use-cases/CU02 – Iniciar Gestión.md` | pending | — |
| `docs/100-business/102-use-cases/CU03 – Lista documentos y certificados necesarios.md` | pending | — |
| `docs/100-business/102-use-cases/CU04 – Registrar documentación cliente.md` | pending | — |
| `docs/100-business/102-use-cases/CU72 – Gestión de Documentos Presentados.md` | pending | — |
| `backend-api/openapi/openapi.yaml` | pending | — |
| `docs/300-development/303-testing/E2E-TEST-MAPPING.md` | pending | — |
| `CHANGELOG.md` | pending | — |

## Gate log

| Gate | Condition | Passed | Evidence |
|------|-----------|--------|----------|
| 1 | Issue + Specification + Acceptance Criteria | yes (draft) | this change + store pack; validate-sdlc-plan |
| 2 | Failing tests written, test cases designed | pending | — |
| 3 | Suite green, coverage held, docs updated | pending | — |
| 4 | CI green, review approved, no conflicts | pending | — |
| 5 | Deployed, smoke test passed, Issue closed | pending | — |

## Exceptions

None for Gate 1. Product PR / CHANGELOG / heavy CI deferred until #1518 merges
or foreman clears (fleet serialize). Prep-only stockpile — no §12 exception.

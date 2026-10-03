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
| Issue | #1054 | open / in progress |
| Use Case | CU15 – Procesar pago; CU20 – Dar alta usuario / CU21 – Modificar Usuario (exemplars); CU26–CU30 surface | exists |
| Related | #945 (Persona extractApiError — shipped); #615 (E2E validation coverage); #1053 (session 401 — shipped) | referenced |
| Specification | `openspec/changes/fix-1054-api-error-toasts/` | Gate 1 complete |
| Branch | `cursor/fix-1054-api-error-toasts-69d3` | active |
| Tasks | `tasks.md` | implementing |
| Commits | `3d560ca3` | recorded |
| Pull Request | https://github.com/matiaspakua/notaire/pull/1145 | draft |
| CI run | pending (watching heavy gates) | pending |

| Merge commit | — | pending |
| Release / tag | — | pending |
| Smoke test | — | pending |

## Requirement coverage

| Scenario (Acceptance Criterion) | Test | Status |
|---------------------------------|------|--------|
| Business validation message is toasted | `mutation-error.test.ts`, `utils.test.ts` | written |
| Fallback when body has no message | `mutation-error.test.ts` | written |
| Authenticated 401 is not a mutation toast | `mutation-error.test.ts` + session-expiry suite | written |
| Delete failure is an ApiError | `api-client.test.ts` (400/409 DELETE bodies) | written |
| Listed page surfaces server message | 15 pages + E2E `TS-0095` | written |
| Field detail maps to FormField error | `mutation-error.test.ts` + E2E usuarios | written |
| Unmapped field detail still toasts | `mutation-error.test.ts` | written |

## Permanent documentation updated

| Document | Updated | Commit |
|----------|---------|--------|
| `docs/100-business/102-use-cases/CU15 – Procesar pago.md` | yes | pending |
| `docs/100-business/102-use-cases/CU20 – Dar alta usuario.md` | yes | pending |
| `docs/300-development/303-testing/E2E-TEST-MAPPING.md` | yes (TS-0095) | pending |
| `CHANGELOG.md` | yes | pending |

## Gate log

| Gate | Condition | Passed | Evidence |
|------|-----------|--------|----------|
| 1 | Issue + Specification + Acceptance Criteria | yes | `bash scripts/validate-sdlc-plan.sh fix-1054-api-error-toasts` |
| 2 | Failing tests written, test cases designed | yes | red then green on focused vitest files |
| 3 | Suite green, coverage held, docs updated | in progress | frontend unit green locally |
| 4 | CI green, review approved, no conflicts | pending | — |
| 5 | Deployed, smoke test passed, Issue closed | pending | — |

## Exceptions

None. Implementation started after #1137 merged to `main`.

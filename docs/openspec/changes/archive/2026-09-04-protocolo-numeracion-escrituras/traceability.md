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
| Issue | #839 | closed (umbrella issue; all 5 sub-changes merged) |
| Use Case | CU86 – Controlar Numeración Correlativa de Escrituras | exists |
| Specification | `openspec/changes/protocolo-numeracion-escrituras/` | complete |
| Branch | `feat/839_protocolo-numeracion-escrituras` | merged |
| Tasks | `tasks.md` | complete |
| Commits | `0ad28298`, `09d7d830`, `ec514335`, `39c4378f`, `baa9f789`, `63c1e417` | done |
| Pull Request | [#942](https://github.com/matiaspakua/notaire/pull/942) | merged |
| CI run | https://github.com/matiaspakua/notaire/actions/runs/33905062151 (CI green); Playwright E2E — Full Suite run 33905062087 failed on 3 pre-existing tests unrelated to this change (Dialog viewport-overflow bug), fixed separately in #947 | done (with noted follow-up) |
| Merge commit | `3fd8022f` | done |
| Release / tag | Continuous deploy on merge to main | done |
| Smoke test | `mvn verify` + backend/frontend CI green on PR #942; E2E Full Suite gap tracked and closed via issue #947 | done |

## Requirement coverage

| Scenario (Acceptance Criterion) | Test | Status |
|---------------------------------|------|--------|
| Número coincide con el correlativo esperado | `NumeracionEscrituraServiceTest#shouldAcceptNumberMatchingExpectedCorrelativo` | passing |
| Número ya utilizado | `EscrituraControllerTest#shouldRejectDuplicateNumeroWithinScope` | passing |
| Salto sin justificación | `EscrituraControllerTest#shouldRejectGapWithoutJustification` | passing |
| Salto con justificación | `EscrituraControllerTest#shouldAcceptGapWithJustification` | passing |
| Escritura de Protocolo Auxiliar no afecta la numeración del Principal | `NumeracionEscrituraServiceTest#shouldKeepAuxiliarNumberingIndependentFromPrincipal` | passing |

## Permanent documentation updated

| Document | Updated | Commit |
|----------|---------|--------|
| `docs/100-business/102-use-cases/CU86 – Controlar Numeración Correlativa de Escrituras.md` | yes | included in PR #942 |
| `CHANGELOG.md` | yes | included in PR #942 |

## Gate log

| Gate | Condition | Passed | Evidence |
|------|-----------|--------|----------|
| 1 | Issue + Specification + Acceptance Criteria | yes | Issue #839, `proposal.md`, `specs/numeracion-escrituras/spec.md` |
| 2 | Failing tests written, test cases designed | yes | TDD commits in PR #942 |
| 3 | Suite green, coverage held, docs updated | yes | `mvn verify` green, CHANGELOG + CU86 doc updated |
| 4 | CI green, review approved, no conflicts | yes (with noted E2E follow-up) | CI run 33905062151 green; E2E Full Suite pre-existing failure fixed via #947 |
| 5 | Deployed, smoke test passed, Issue closed | yes | Merge commit `3fd8022f`, Issue #839 closed |

## Exceptions

None.

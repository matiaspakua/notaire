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
| Issue | #839 | open |
| Use Case | CU86 – Controlar Numeración Correlativa de Escrituras | exists (new, created during triage) |
| Specification | `openspec/changes/protocolo-numeracion-escrituras/` | in progress |
| Branch | `feat/839_protocolo-numeracion-escrituras` | pending |
| Tasks | `tasks.md` | 0/N complete |
| Commits | — | pending |
| Pull Request | — | pending |
| CI run | — | pending |
| Merge commit | — | pending |
| Release / tag | — | pending |
| Smoke test | — | pending |

## Requirement coverage

| Scenario (Acceptance Criterion) | Test | Status |
|---------------------------------|------|--------|
| Número coincide con el correlativo esperado | `NumeracionEscrituraServiceTest#shouldAcceptNumberMatchingExpectedCorrelativo` | pending |
| Número ya utilizado | `EscrituraControllerTest#shouldRejectDuplicateNumeroWithinScope` | pending |
| Salto sin justificación | `EscrituraControllerTest#shouldRejectGapWithoutJustification` | pending |
| Salto con justificación | `EscrituraControllerTest#shouldAcceptGapWithJustification` | pending |
| Escritura de Protocolo Auxiliar no afecta la numeración del Principal | `NumeracionEscrituraServiceTest#shouldKeepAuxiliarNumberingIndependentFromPrincipal` | pending |

## Permanent documentation updated

| Document | Updated | Commit |
|----------|---------|--------|
| `docs/100-business/102-use-cases/CU86 – Controlar Numeración Correlativa de Escrituras.md` | no | — |
| `CHANGELOG.md` | no | — |

## Gate log

| Gate | Condition | Passed | Evidence |
|------|-----------|--------|----------|
| 1 | Issue + Specification + Acceptance Criteria | no | in progress |
| 2 | Failing tests written, test cases designed | no | — |
| 3 | Suite green, coverage held, docs updated | no | — |
| 4 | CI green, review approved, no conflicts | no | — |
| 5 | Deployed, smoke test passed, Issue closed | no | — |

## Exceptions

None.

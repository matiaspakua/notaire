# Traceability

> Governed by [CONSTITUTION.md](../../../CONSTITUTION.md) — P4 Traceability.

## Chain

```text
Issue → Specification → Tasks → Commits → PR → Merge → Release
```

| Link | Reference | Status |
|------|-----------|--------|
| Issue | #851 | open → in progress |
| Use Case | CU44 – Reingresar testimonio (#197) | exists |
| Related | #832 (original reenter), #197 (CU44) | referenced |
| Specification | `openspec/changes/feat-851-reingreso-testimonio-data/` | Gate 1 draft |
| Branch | `feat/851_reingreso_testimonio` | created from updated `main` |
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
| A reentry with data | `TestimonyMovementServiceTest` | pending |
| A reentry without a body | `TestimonyMovementControllerIntegrationTest` | pending |
| Observed without notes | `TestimonyMovementServiceTest` | pending |
| The reentry dialog | `testing/e2e/tests/TS-0032-testimonio-inscripcion-feature.spec.ts` | pending |

## Permanent documentation updated

| Document | Updated | Commit |
|----------|---------|--------|
| `docs/100-business/102-use-cases/CU44 – Reingresar testimonio.md` | pending | — |
| `docs/200-architecture/205-data-model/Diccionario de Datos.md` | pending | — |
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

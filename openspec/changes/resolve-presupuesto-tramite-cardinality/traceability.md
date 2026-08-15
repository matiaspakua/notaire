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
| Issue | #798 | open |
| Use Case | CU01 – Preparar Presupuesto, CU45 – Modificar presupuesto | exists |
| Specification | `openspec/changes/resolve-presupuesto-tramite-cardinality/` | drafted |
| Branch | `fix/798_resolve-presupuesto-tramite-cardinality` | created |
| Tasks | `tasks.md` | groups 1-9 complete; 10-12 in progress |
| Commits | `62754f7` (style: LF normalize), `b72d07f` (fix: cardinality), `f5a34f3` (chore: exclude frontend-swing), `cfc5743` (docs: baseline + changelog), `1782ab4` (test: complete-case Tramite->Presupuesto assertion), `863c2b8` (docs: OpenSpec change artifacts) | done |
| Pull Request | | pending |
| CI run | | pending |
| Merge commit | | pending |
| Release / tag | | pending |
| Smoke test | | pending |

## Requirement coverage

| Scenario (Acceptance Criterion) | Test | Status |
|---------------------------------|------|--------|
| A Tramite is created and associated to a Presupuesto | `TramiteSerializationIntegrationTest.shouldListTramitesWithoutCyclicRecursion` | passing |
| A Presupuesto is associated with more than one Tramite | `PresupuestoEntityTest` (tramiteList assertions) | passing |
| A Tramite belongs to at most one Presupuesto | `TramiteEntityTest` | passing |
| Presupuesto no longer exposes a single-Tramite reference | `PresupuestoEntityTest`, `RemainingControllersJpaTest` | passing |
| Migration refuses to drop the column if data would be lost | `PresupuestoTramiteMigrationGuardIntegrationTest.v14FailsWhenLegacyColumnHoldsData` | passing |

## Permanent documentation updated

| Document | Updated | Commit |
|----------|---------|--------|
| `docs/01-business/00-FUNCTIONAL-BASELINE.md` | yes | `cfc5743` |
| `CHANGELOG.md` | yes | `cfc5743` |

## Gate log

| Gate | Condition | Passed | Evidence |
|------|-----------|--------|----------|
| 1 | Issue + Specification + Acceptance Criteria | yes | proposal.md and specs/presupuesto-tramite-relation/spec.md drafted; Issue #798 open and labeled |
| 2 | Failing tests written, test cases designed | yes | Tests written per design.md Testing Strategy, observed failing pre-implementation, now passing |
| 3 | Suite green, coverage held, docs updated | yes | `mvn test -pl backend-api`: 1472/1472 passing; `bash scripts/preflight.sh --fix`: 13 passed, 1 skipped (Playwright/Bruno, CI-only) |
| 4 | CI green, review approved, no conflicts | pending | |
| 5 | Deployed, smoke test passed, Issue closed | pending | |

## Exceptions

None.

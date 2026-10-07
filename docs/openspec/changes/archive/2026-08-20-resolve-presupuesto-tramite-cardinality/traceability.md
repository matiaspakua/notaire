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
| Issue | #798 | open (not closed — see Exceptions) |
| Use Case | CU01 – Preparar Presupuesto, CU45 – Modificar presupuesto | exists |
| Specification | `openspec/changes/resolve-presupuesto-tramite-cardinality/`; synced to `openspec/specs/presupuesto-tramite-relation/spec.md` | done |
| Branch | `fix/798_resolve-presupuesto-tramite-cardinality` | merged (deleted post-merge) |
| Tasks | `tasks.md` | groups 1-9 complete; 10-12 not checked off (see Exceptions) |
| Commits | `62754f7` (style: LF normalize), `b72d07f` (fix: cardinality), `f5a34f3` (chore: exclude frontend-swing), `cfc5743` (docs: baseline + changelog), `1782ab4` (test: complete-case Tramite->Presupuesto assertion), `863c2b8` (docs: OpenSpec change artifacts) | done |
| Pull Request | #808 | merged 2026-08-15T10:37:10Z |
| CI run | Playwright E2E — Full Suite: failure; Performance k6 Load Test: failure; Test Coverage Report: success (on merge commit `b29e425`) | not fully green |
| Merge commit | `b29e425ec06fb750d101b59682866d18c11ebdbd` | done |
| Release / tag | none found | pending |
| Smoke test | none run | pending |

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
| 4 | CI green, review approved, no conflicts | no (exception) | PR #808 merged 2026-08-15; Playwright E2E Full Suite and k6 Performance Load Test failed on merge commit `b29e425` |
| 5 | Deployed, smoke test passed, Issue closed | no (exception) | No CD/deploy run found for `b29e425`; no smoke test run; Issue #798 remains open |

## Exceptions

**Archived without Gates 4-5 fully evidenced — explicit user approval, 2026-08-20.**
The code change (PR #808) merged into `main` on 2026-08-15 and was independently
confirmed to be fully present in `origin/main` (rebasing this branch onto
`origin/main` produced zero diff — every commit was either already upstream or
became redundant). What remains unverified/undone at archive time:
- CI on the merge commit shows Playwright E2E and k6 load test failures (not
  re-investigated to confirm pre-existing/unrelated vs. caused by this change).
- No CD/deploy run or smoke test was found for the merge commit.
- Issue #798 was not closed.

User explicitly chose "Archive anyway" after being shown this gap
(see conversation), accepting the risk that Gate 4/5 evidence is incomplete.
Follow-up: verify the CI failures are unrelated (e.g. compare against known
flaky/pre-existing failures like #827), close Issue #798, and confirm a
deployment + smoke test occurred for a build containing `b29e425`.

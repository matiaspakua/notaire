# Traceability

> Governed by [CONSTITUTION.md](../../../CONSTITUTION.md) — P4 Traceability.

## Chain

```text
Issue → Specification → Tasks → Commits → PR → Merge → Release
```

| Link | Reference | Status |
|------|-----------|--------|
| Issue | #802 | open → in progress |
| Use Case | CU42 – Informar próximos vencimientos (#195) | exists |
| Related | #837 (due date inheritance), #195 (CU42) | referenced |
| Specification | `openspec/changes/feat-802-proximos-vencimientos/` | Gate 1 draft |
| Branch | `feat/802_proximos_vencimientos` | created from updated `main` |
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
| A document due inside the window | `UpcomingExpirationServiceTest` | pending |
| Several documents inside the window | `UpcomingExpirationServiceTest` | pending |
| Outside or irrelevant documents | `UpcomingExpirationControllerIntegrationTest` | pending |
| Invalid window | `UpcomingExpirationControllerIntegrationTest` | pending |
| Default window | `UpcomingExpirationServiceTest` | pending |
| The screen lists the documents | `testing/e2e/tests/TS-0097-proximos-vencimientos-feature.spec.ts` | pending |

## Permanent documentation updated

| Document | Updated | Commit |
|----------|---------|--------|
| `docs/100-business/102-use-cases/CU42 – Informar próximos vencimientos.md` | pending | — |
| `backend-api/openapi/openapi.yaml` | pending | — |
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

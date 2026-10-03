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
| Issue | #799 | open (in-progress label ACL denied for bot) |
| Use Case | CU17, CU18 | exists |
| Related | #835 (service-layer duplicate reject — shipped) | referenced |
| Specification | `openspec/changes/fix-799-persona-id-uniqueness/` | Gate 1 complete |
| Branch | `cursor/fix-799-persona-id-uniqueness-69d3` | active |
| Tasks | `tasks.md` | planning done; implementation pending |
| Commits | — | pending |
| Pull Request | — | pending |
| CI run | pending (coordinator: `bash scripts/check-heavy-ci.sh <pr>`) | pending |
| Merge commit | — | pending |
| Release / tag | — | pending |
| Smoke test | — | pending |

## Requirement coverage

| Scenario (Acceptance Criterion) | Test | Status |
|---------------------------------|------|--------|
| Second insert with same type and number is rejected by the database | `PersonIdentificationUniquenessIntegrationTest#shouldRejectSecondInsertWithSameTypeAndNumber` | pending |
| Migration refuses to proceed when duplicate groups exist | `PersonIdentificationUniquenessPgIntegrationTest` / V40 DO block on Flyway apply | pending |
| Concurrent create race still surfaces as HTTP 409 | `PersonServiceTest` race mapping / uniqueness IT | pending |
| Alta exitosa con documento no registrado | existing `PersonServiceTest` (#835) | covered (existing) |
| Rechazo de alta con documento ya registrado | existing `PersonServiceTest` (#835) | covered (existing) |

## Permanent documentation updated

| Document | Updated | Commit |
|----------|---------|--------|
| `docs/100-business/102-use-cases/CU17 – Dar Alta persona.md` | pending | — |
| `docs/100-business/102-use-cases/CU18 – Dar Alta Cliente.md` | pending | — |
| `CHANGELOG.md` | pending | — |

## Gate log

| Gate | Condition | Passed | Evidence |
|------|-----------|--------|----------|
| 1 | Issue + Specification + Acceptance Criteria | yes | `bash scripts/validate-sdlc-plan.sh fix-799-persona-id-uniqueness` |
| 2 | Failing tests written, test cases designed | pending | — |
| 3 | Suite green, coverage held, docs updated | pending | — |
| 4 | CI green, review approved, no conflicts | pending | — |
| 5 | Deployed, smoke test passed, Issue closed | pending | — |

## Exceptions

None. `in-progress` label could not be applied (GitHub GraphQL: Resource not accessible by integration).

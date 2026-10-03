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
| Issue | #1065 | open (in-progress label API denied) |
| Use Case | CU76 — Quality Assurance and Testing Infrastructure | exists |
| Specification | `openspec/changes/api-1065-rest-conventions/` | Gate 1 filled |
| Branch | `cursor/api-1065-rest-conventions-69d3` | created |
| Tasks | `tasks.md` | 0/N complete (impl pending) |
| Commits | | pending |
| Pull Request | | pending |
| CI run | | pending |
| Merge commit | | pending |
| Release / tag | | pending |
| Smoke test | | pending |

## Requirement coverage

| Scenario (Acceptance Criterion) | Test | Status |
|---------------------------------|------|--------|
| Registration draft create returns 201 and Location | `RegistrationDraftControllerTest` | pending |
| Sample payment create includes Location | `PaymentControllerTest` | pending |
| Sample folio create includes Location | Folio create test | pending |
| Payment params create is absent | `PaymentControllerTest` | pending |
| ADR-023 exists and states English resource nouns for new paths | ADR-023 file + README | pending |
| JSON payment create includes Location header | `PaymentControllerTest` | pending |
| Generar minuta con datos completos (201+Location) | `RegistrationDraftControllerTest` | pending |
| Generar minuta / incompletos / PDF scenarios (unchanged behavior) | existing `RegistrationDraftControllerTest` / report tests | pending (existing) |
| Processing/retrieving/editing payment method scenarios | existing `PaymentControllerTest` / service tests | pending (existing) |

## Permanent documentation updated

| Document | Updated | Commit |
|----------|---------|--------|
| `docs/200-architecture/202-ADR/ADR-023-rest-resource-naming.md` | no | pending |
| `docs/200-architecture/202-ADR/README.md` | no | pending |
| `docs/200-architecture/203-design/REST-API-ENDPOINT_REGISTRY.md` | no | pending |
| `CHANGELOG.md` | no | pending |

## Gate log

| Gate | Condition | Passed | Evidence |
|------|-----------|--------|----------|
| 1 | Issue + Specification + Acceptance Criteria | pending validate | `bash scripts/validate-sdlc-plan.sh api-1065-rest-conventions` |
| 2 | Failing tests written, test cases designed | pending | |
| 3 | Suite green, coverage held, docs updated | pending | |
| 4 | CI green, review approved, no conflicts | pending | |
| 5 | Deployed, smoke test passed, Issue closed | pending | |

## Exceptions

Issue label `in-progress` could not be applied (`Resource not accessible by
integration`). Work proceeds on the assigned branch; status file records the
blocker.

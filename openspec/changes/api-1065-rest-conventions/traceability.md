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
| Specification | `openspec/changes/api-1065-rest-conventions/` | Gate 1 passed |
| Branch | `cursor/api-1065-rest-conventions-69d3` | pushed |
| Tasks | `tasks.md` | impl + local gates done |
| Commits | `9b2e1c89` docs(openspec); `b4e40b97` feat(api); `9a938421` tasks/traceability | done |
| Pull Request | [#1182](https://github.com/matiaspakua/notaire/pull/1182) | draft |
| CI run | | pending |
| Merge commit | | pending |
| Release / tag | | pending |
| Smoke test | | pending |

## Requirement coverage

| Scenario (Acceptance Criterion) | Test | Status |
|---------------------------------|------|--------|
| Registration draft create returns 201 and Location | `RegistrationDraftControllerTest#shouldGenerateDraftWhenDataIsComplete` | passing |
| Sample payment create includes Location | `PaymentControllerTest#shouldCreatePaymentViaJson` | passing |
| Sample folio create includes Location | `FolioControllerTest#shouldCreateFolioWithStatusNuevo` | passing |
| Payment params create is absent | `PaymentControllerTest#shouldNotExposePaymentParamsCreateRoute` | passing |
| ADR-023 exists and states English resource nouns for new paths | `docs/200-architecture/202-ADR/ADR-023-rest-resource-naming.md` | done |
| JSON payment create includes Location header | `PaymentControllerTest#shouldCreatePaymentViaJson` | passing |
| Generar minuta con datos completos (201+Location) | `RegistrationDraftControllerTest#shouldGenerateDraftWhenDataIsComplete` | passing |
| CreatedResponses helper builds Location | `CreatedResponsesTest` | passing |

## Permanent documentation updated

| Document | Updated | Commit |
|----------|---------|--------|
| `docs/200-architecture/202-ADR/ADR-023-rest-resource-naming.md` | yes | `b4e40b97` |
| `docs/200-architecture/202-ADR/README.md` | yes | `b4e40b97` |
| `docs/200-architecture/203-design/REST-API-ENDPOINT_REGISTRY.md` | yes | `b4e40b97` |
| `CHANGELOG.md` | yes | `b4e40b97` |
| `backend-api/openapi/openapi.yaml` | yes (params removed) | `b4e40b97` |

## Gate log

| Gate | Condition | Passed | Evidence |
|------|-----------|--------|----------|
| 1 | Issue + Specification + Acceptance Criteria | yes | `bash scripts/validate-sdlc-plan.sh api-1065-rest-conventions` |
| 2 | Failing tests written, test cases designed | yes | compile miss + red assertions before impl |
| 3 | Suite green, coverage held, docs updated | yes local | `mvn verify -pl backend-api -am`; Bruno payments 22/22 |
| 4 | CI green, review approved, no conflicts | pending | PR #1182 draft — do not merge |
| 5 | Deployed, smoke test passed, Issue closed | pending | |

## Exceptions

Issue label `in-progress` / comments could not be applied (`Resource not
accessible by integration`). Work proceeds on the assigned branch; status file
records the PR URL and head SHA.

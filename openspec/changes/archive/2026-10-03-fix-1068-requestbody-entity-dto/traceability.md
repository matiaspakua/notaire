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
| Issue | #1068 | open (IN PROGRESS label blocked: `gh` write denied for this agent) |
| Use Case | CU78 — Security, Privacy and Compliance | exists on issue |
| Specification | `openspec/changes/fix-1068-requestbody-entity-dto/` | Gate 1 passed |
| Branch | `cursor/fix-1068-requestbody-dto-69d3` | created (cloud prefix; Constitution form would be `fix/1068_requestbody_dto`) |
| Tasks | `tasks.md` | 0/N complete |
| Commits | | pending |
| Pull Request | | pending |
| CI run | | pending |
| Merge commit | | pending |
| Release / tag | | pending |
| Smoke test | | pending |

## Requirement coverage

| Scenario (Acceptance Criterion) | Test | Status |
|---------------------------------|------|--------|
| Property create ignores client-supplied id and version | `RequestBodyDtoBindingIntegrationTest#shouldIgnoreClientIdAndVersionOnPropertyCreate` | pending |
| Person update uses path id and existing version | `RequestBodyDtoBindingIntegrationTest#shouldUpdatePersonFromRequestDtoWithoutClientVersion` | pending |
| Invalid person request is rejected | `PersonRequestValidationIntegrationTest#shouldRejectCreateWithBlankFirstName` | exists |
| Budget create accepts only writable budget fields | `RequestBodyDtoBindingIntegrationTest#shouldCreateBudgetFromRequestDtoIgnoringServerFields` | pending |
| Created resource returns response DTO with generated id | `RequestBodyDtoBindingIntegrationTest#shouldReturnPropertyResponseDtoWithGeneratedId` | pending |
| Audit-log POST is rejected | `AuditRecordMutationDisabledIntegrationTest#shouldRejectAuditLogPost` | pending |

## Permanent documentation updated

| Document | Updated | Commit |
|----------|---------|--------|
| OpenAPI annotations on affected controllers | pending | |
| Bruno collections (if present for these routes) | pending | |
| `CHANGELOG.md` | pending | |

## Gate log

| Gate | Condition | Passed | Evidence |
|------|-----------|--------|----------|
| 1 | Issue + Specification + Acceptance Criteria | yes | `openspec validate --strict` + `validate-sdlc-plan.sh` exit 0 |
| 2 | Failing tests written, test cases designed | pending | |
| 3 | Suite green, coverage held, docs updated | pending | |
| 4 | CI green, review approved, no conflicts | pending | |
| 5 | Deployed, smoke test passed, Issue closed | pending | |

## Exceptions

Label `in-progress` could not be applied (`gh` GraphQL: Resource not accessible by integration); recorded here, not treated as a Constitution §12 process exception. Cloud branch naming uses `cursor/fix-1068-requestbody-dto-69d3` as required by the agent platform.

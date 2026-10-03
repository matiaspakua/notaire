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
| Issue | #1060 | in-progress |
| Use Case | CU23, CU78, CU73 | exists |
| Specification | `openspec/changes/security-1060-audit-log-mutations-denied/` | seeded |
| Branch | `cursor/fix-1060-audit-log-mutations-69d3` | created |
| Tasks | `tasks.md` | 0/N complete |
| Commits |  | pushed |
| Pull Request | [#1128](https://github.com/matiaspakua/notaire/pull/1128) | open (draft) |
| CI run | pending | pending |
| Merge commit | pending | pending |
| Release / tag | pending | pending |
| Smoke test | pending | pending |

## Requirement coverage

| Scenario (Acceptance Criterion) | Test | Status |
|---------------------------------|------|--------|
| POST audit-log is rejected | `AuditRecordControllerTest#shouldRejectCreateOfAuditRecords`; `AuditRecordMutationDisabledIntegrationTest#shouldRejectAuditLogPost`; Bruno `02-post-denied.yml` | pending |
| PUT audit-log is rejected | `AuditRecordControllerTest#shouldRejectUpdateOfAuditRecords`; `AuditRecordMutationDisabledIntegrationTest#shouldRejectAuditLogPut`; Bruno `03-put-denied.yml` | pending |
| DELETE audit-log is rejected | `AuditRecordControllerTest#shouldRejectDeleteOfAuditRecords`; `AuditRecordMutationDisabledIntegrationTest#shouldRejectAuditLogDelete`; Bruno `04-delete-denied.yml` | pending |
| OpenAPI tag is consult-only | `@Tag` on `AuditRecordController` + Gate 3 Swagger check | pending |
| Bruno rejects audit-log mutations | `api-test/audit-records/02|03|04-*-denied.yml` | pending |

## Permanent documentation updated

| Document | Updated | Commit |
|----------|---------|--------|
| `docs/200-architecture/206-security/THREAT-MODEL-AUTH-AUDIT.md` | pending | pending |
| `CHANGELOG.md` | pending | pending |
| `backend-api/api-test/COVERAGE.md` | pending | pending |

## Gate log

| Gate | Condition | Passed | Evidence |
|------|-----------|--------|----------|
| 1 | Issue + Specification + Acceptance Criteria | yes | validate-sdlc-plan.sh security-1060… |
| 2 | Failing tests written, test cases designed | yes | PUT/tag tests + Bruno |
| 3 | Suite green, coverage held, docs updated | pending | pending |
| 4 | CI green, review approved, no conflicts | pending | pending |
| 5 | Deployed, smoke test passed, Issue closed | pending | pending |

## Exceptions

Label `in-progress` could not be applied via `gh` (GraphQL: Resource not accessible by integration). Work proceeds on branch; PR will close #1060.

# Traceability

> Governed by [CONSTITUTION.md](../../../../CONSTITUTION.md) — P4 Traceability.
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
| Issue | #<!-- number --> | <!-- open / in-progress / closed --> |
| Use Case | CU82 — Generar Minuta de Inscripción / RF-91 | exists |
| Specification | `docs/openspec/changes/feat-116-registration-draft-report/` | passed |
| Branch | `cursor/116-registration-draft-5fcb` | created |
| Tasks | `tasks.md` | in progress |
| Commits | pending | pending |
| Pull Request | pending | pending |
| CI run | pending | pending |
| Merge commit | pending | pending |
| Release / tag | pending | pending |
| Smoke test | pending | pending |

## Requirement coverage

<!-- Every `#### Scenario:` in the delta spec is an Acceptance Criterion and must
     appear here with the test that proves it. Requirement coverage is not code
     coverage: a green suite with an unmapped scenario means the criterion is
     unverified. -->

| Scenario (Acceptance Criterion) | Test | Status |
|---------------------------------|------|--------|
| Generated draft prints recorded cadastral designation | RegistrationDraftReportAdapterTest | passing |
| Missing optional fields stay N/A | RegistrationDraftReportAdapterTest | passing |
| Unknown draft is 404 | RegistrationDraftReportAdapterTest | passing |
| Registry notes appear | RegistrationDraftReportAdapterTest | passing |
| Download contains cadastral text | RegistrationDraftReportAdapterTest | passing |

## Permanent documentation updated

<!-- Mirror of proposal.md — Documentation Impact, confirmed at Gate 3.
     Reference the documents; do not restate their content. -->

| Document | Updated | Commit |
|----------|---------|--------|
| `docs/100-business/102-use-cases/CU82 – Generar Minuta de Inscripción.md` | no | pending |
| `docs/200-architecture/201-SAD/pdf-report-design-system.md` | no | pending |
| `docs/200-architecture/203-design/REST-API-REFERENCE.md` | no | pending |
| `docs/300-development/303-testing/E2E-TEST-MAPPING.md` | no | pending |
| `docs/300-development/303-testing/INVENTORY.md` | no | pending |
| `CHANGELOG.md` | no | pending |

## Gate log

| Gate | Condition | Passed | Evidence |
|------|-----------|--------|----------|
| 1 | Issue + Specification + Acceptance Criteria | yes | #116, this folder |
| 2 | Failing tests written, test cases designed | pending | pending |
| 3 | Suite green, coverage held, docs updated | pending | pending |
| 4 | CI green, review approved, no conflicts | pending | pending |
| 5 | Deployed, smoke test passed, Issue closed | pending | pending |

## Exceptions

Issue #116 is OPEN. Adding `in-progress` / Delivery Board In Progress returns
HTTP 403 from this integration. Work proceeds on the existing RF-91 issue.
CU82 doc GitHub ID `#313` is a PR, not this issue. This slice does not invent
Owner official-form geometry, signature language, or grantor/grantee roles.


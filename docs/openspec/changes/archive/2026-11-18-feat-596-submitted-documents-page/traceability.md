# Traceability

> Governed by [CONSTITUTION.md](../../../../CONSTITUTION.md) — P4 Traceability.

## Chain

```text
Issue → Specification → Tasks → Commits → PR → Merge → Release
```

| Link | Reference | Status |
|------|-----------|--------|
| Issue | #596 | open |
| Use Case | CU72 — Submitted documents | exists |
| Specification | `docs/openspec/changes/feat-596-submitted-documents-page/` | drafted |
| Branch | `cursor/596-submitted-documents-page-47dc` | created |
| Tasks | `tasks.md` | pending |
| Commits | pending | pending |
| Pull Request | pending | pending |
| CI run | pending | pending |
| Merge commit | pending | pending |
| Release / tag | pending | pending |
| Smoke test | pending | pending |

## Requirement coverage

| Scenario (Acceptance Criterion) | Test | Status |
|---------------------------------|------|--------|
| The list body has content | `SubmittedDocumentControllerTest` | pending preflight |
| The created document is inside content | `submitted-documents/02-list.yml` | pending preflight |

## Permanent documentation updated

| Document | Updated | Commit |
|----------|---------|--------|
| `backend-api/openapi/openapi.yaml` | yes | pending |

## Gate log

| Gate | Condition | Passed | Evidence |
|------|-----------|--------|----------|
| 1 | Issue + Specification | yes | validate after fill |
| 2 | Page assertion | yes | test reads content |
| 3 | Suite green | no | pending preflight |
| 4 | Review | no | pending |
| 5 | Owner merge | no | #596 stays open |

## Exceptions

No exception. Label writes return 403.

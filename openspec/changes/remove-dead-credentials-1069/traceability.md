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
| Issue | #1069 | in-progress |
| Use Case | CU78 — Security, Privacy and Compliance | registered |
| Specification | `openspec/changes/remove-dead-credentials-1069/` | written |
| Branch | `chore/1069_remove_dead_credentials` | created |
| Tasks | `tasks.md` | in progress |
| Commits | pending | pending |
| Pull Request | pending | pending |
| CI run | pending | pending |
| Merge commit | pending | pending |
| Release / tag | pending | pending |
| Smoke test | pending | pending |

## Requirement coverage

### Issue → Use Case → Requirement → Scenario → Planned Test → Planned File

| Issue (CR) | Use Case | Requirement | Scenario | Planned Test | Planned File |
|------------|----------|-------------|----------|--------------|--------------|
| #1069 | CU78 — Security, Privacy and Compliance | dead security user keys must not exist | WHEN config loaded from application.properties THEN no key starts with "spring.security.user." | ApplicationPropertiesHygieneTest | backend-api/src/main/java/com/licensis/notaire/api/config/ApplicationPropertiesHygiene.java |
| #1069 | CU78 — Security, Privacy and Compliance | legacy config files must be absent | WHEN resource "/config.properties" is looked up THEN resource is absent | ApplicationResourceTest | backend-api/src/main/java/com/licensis/notaire/api/config/ApplicationResource.java |

## Permanent documentation updated

| Document | Updated | Commit |
|----------|---------|--------|
| CHANGELOG.md | n/a - not user visible | pending |

## Gate log

| Gate | Condition | Passed | Evidence |
|------|-----------|--------|----------|
| 1 | Issue + Specification + Acceptance Criteria | passed | Issue #1069; this `openspec/changes/remove-dead-credentials-1069/`; spec validation `openspec validate` passed |

## Exceptions

None at this time.

# Traceability

> Governed by [CONSTITUTION.md](../../../../CONSTITUTION.md) — P4 Traceability.

## Chain

```text
Issue → Specification → Tasks → Commits → PR → Merge → Release
```

| Link | Reference | Status |
|------|-----------|--------|
| Issue | #2351 | open |
| Use Case | CU76 — Quality Assurance and Testing Infrastructure | exists |
| Specification | `docs/openspec/changes/fix-2351-login-attempt-entity/` | passed |
| Branch | `cursor/2351-login-attempt-entity-47dc` | created |
| Tasks | `tasks.md` | in progress |
| Commits | pending | pending |
| Pull Request | pending | pending |
| CI run | pending | pending |
| Merge commit | pending | pending |
| Release / tag | pending | pending |
| Smoke test | pending | pending |

## Requirement coverage

| Scenario (Acceptance Criterion) | Test | Status |
|---------------------------------|------|--------|
| the shared business package does not contain the entity | LoginAttemptEntityLocationTest | passing |

## Permanent documentation updated

| Document | Updated | Commit |
|----------|---------|--------|
| none beyond this change | n/a | n/a |

## Gate log

| Gate | Condition | Passed | Evidence |
|------|-----------|--------|----------|
| 1 | Issue + Specification + Acceptance Criteria | yes | #2351, this folder |
| 2 | Location test | yes | LoginAttemptEntityLocationTest |
| 3 | Suite green | pending | preflight |
| 4 | CI green | pending | pending |
| 5 | Issue closed | pending | the issue stays open |

## Exceptions

None.

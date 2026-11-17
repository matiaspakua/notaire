# Traceability

> Governed by [CONSTITUTION.md](../../../../CONSTITUTION.md) — P4 Traceability.

## Chain

```text
Issue → Specification → Tasks → Commits → PR → Merge → Release
```

| Link | Reference | Status |
|------|-----------|--------|
| Issue | #580 | open |
| Use Case | CU76 — Quality Assurance and Testing Infrastructure | exists |
| Specification | `docs/openspec/changes/refactor-580-procedure-type-dto/` | drafted |
| Branch | `cursor/580-procedure-type-mapper-47dc` | created |
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
| Fields copy and an invalid DTO is ignored | `ProcedureTypeLegacyMapperTest` | passing |
| The entity has no getDto or setAttributes | `ProcedureTypeLegacyMapperTest` | passing |

## Permanent documentation updated

| Document | Updated | Commit |
|----------|---------|--------|
| None. HTTP API unchanged | n/a | n/a |

## Gate log

| Gate | Condition | Passed | Evidence |
|------|-----------|--------|----------|
| 1 | Issue + Specification | yes | validate after fill |
| 2 | Failing test | yes | testCompile missing ProcedureTypeLegacyMapper |
| 3 | Suite green | no | pending preflight |
| 4 | Review | no | pending |
| 5 | Owner merge | no | #580 stays open |

## Exceptions

No exception. Label writes return 403.

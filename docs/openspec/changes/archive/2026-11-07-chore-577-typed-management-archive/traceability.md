# Traceability

> Governed by [CONSTITUTION.md](../../../../CONSTITUTION.md) — P4 Traceability.

## Chain

```text
Issue → Specification → Tasks → Commits → PR → Merge → Release
```

| Link | Reference | Status |
|------|-----------|--------|
| Issue | #577 | in-progress |
| Use Case | CU16 | exists |
| Specification | this change | passed |
| Branch | `refactor/577_typed-management-archive` | created |
| Tasks | `tasks.md` | in progress |
| Commits | pending | pending |
| Pull Request | pending | pending |
| CI run | pending | pending |
| Merge commit | pending | pending |
| Release / tag | pending | pending |
| Smoke test | pending | pending |

## Requirement coverage

| Scenario | Test | Status |
|----------|------|--------|
| archiving leaves the loose baseline | `ControllerSignatureArchitectureTest` | pending |
| Missing management and waiting folders keep their HTTP bodies | `ManagementArchiveControllerTest` | pending |

## Permanent documentation updated

| Document | Updated | Commit |
|----------|---------|--------|
| `CHANGELOG.md` | pending | pending |
| `docs/openspec/specs/controller-typed-returns/spec.md` | pending | pending |

## Gate log

| Gate | Passed | Evidence |
|------|--------|----------|
| 1 | yes | #577, `validate-sdlc-plan.sh` |

## Exceptions

None. #577 stays open (`Refs #577`).

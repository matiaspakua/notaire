# Traceability

> Governed by [CONSTITUTION.md](../../../../CONSTITUTION.md) — P4 Traceability.

## Chain

```text
Issue → Specification → Tasks → Commits → PR → Merge → Release
```

| Link | Reference | Status |
|------|-----------|--------|
| Issue | #577 | in-progress |
| Use Case | CU84 | exists |
| Specification | this change | passed |
| Branch | `refactor/577_typed-user-login` | created |
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
| login leaves the loose baseline | `ControllerSignatureArchitectureTest` | passing |
| Rejected and locked logins keep their HTTP bodies | `UserLoginControllerTest` | passing |

## Permanent documentation updated

| Document | Updated | Commit |
|----------|---------|--------|
| `CHANGELOG.md` | yes | this PR |
| `docs/openspec/specs/controller-typed-returns/spec.md` | yes | this PR |

## Gate log

| Gate | Passed | Evidence |
|------|--------|----------|
| 1 | yes | #577, `validate-sdlc-plan.sh` |

## Exceptions

None. #577 stays open (`Refs #577`). Logout stays on the baseline because it does not share login's failure shapes.

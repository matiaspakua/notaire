# Traceability

> Governed by [CONSTITUTION.md](../../../../CONSTITUTION.md) — P4 Traceability.
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
| Issue | #2198 | in-progress |
| Use Case | CU76 | exists |
| Specification | `docs/openspec/changes/feat-2198-isolator-os-grant-expire/` | passed |
| Branch | `cursor/feat-2198-isolator-os-grant-expire-234b` | created |
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
| grant expire fields parse and show | GrantExpireTest | pending |
| expired grant fails closed | GrantExpireTest | pending |
| ttl_seconds past fails closed | GrantExpireTest | pending |
| isolator status without bwrap is honest | IsolatorOsTest | pending |
| isolator probe reports backend and sandbox | IsolatorOsTest | pending |
| isolator run refuses when sandbox unavailable | IsolatorOsTest | pending |
| isolator run refuses expired grant | IsolatorOsTest | pending |
| doctor never claims sandbox without probe success | IsolatorOsTest | pending |

## Permanent documentation updated

| Document | Updated | Commit |
|----------|---------|--------|
| `framework/ai-sdlc/docs/adr/ADR-004-isolator-runtime.md` | no | pending |
| `docs/300-development/306-ai-sdlc-framework/FACTORY-FOUNDATION.md` | no | pending |
| `docs/300-development/306-ai-sdlc-framework/FACTORY-ROADMAP.md` | no | pending |
| `framework/ai-sdlc/README.md` | no | pending |

## Gate log

| Gate | Condition | Passed | Evidence |
|------|-----------|--------|----------|
| 1 | Issue + Specification + Acceptance Criteria | yes | #2198, this folder |
| 2 | Failing tests written, test cases designed | pending | pending |
| 3 | Suite green, coverage held, docs updated | pending | pending |
| 4 | CI green, review approved, no conflicts | pending | pending |
| 5 | Deployed, smoke test passed, Issue closed | pending | pending |

## Exceptions

None.

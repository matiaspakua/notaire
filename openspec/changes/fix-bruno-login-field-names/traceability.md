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
| Issue | #1008 | in-progress |
| Use Case | None — CI/CD defect (see proposal.md) | n/a |
| Specification | `openspec/changes/fix-bruno-login-field-names/` | written |
| Branch | `ci/1008_fix-bruno-login-field-names` | created |
| Tasks | `tasks.md` | in progress (groups 1-9 complete) |
| Commits | 1a7c138 | complete |
| Pull Request | — | pending |
| CI run | — | pending |
| Merge commit | — | pending |
| Release / tag | — | pending |
| Smoke test | — | pending |

## Requirement coverage

<!-- skip_specs: true — no delta spec scenarios. Acceptance Criteria are the
     Issue's checklist items instead. -->

| Acceptance Criterion (Issue #1008) | Verification | Status |
|-------------------------------------|------|--------|
| Bruno login step sends `{"name": "admin", "password": "admin"}` | Diff of `.github/workflows/playwright-e2e.yml` in commit 1a7c138 | passing |
| `API Tests (Bruno)` job passes on next `main` push / PR run | GitHub Actions run on this PR | pending |
| No other workflow step makes the same mistake | `grep -rn "contrasenia" .github/workflows/` — clean | passing |

## Permanent documentation updated

| Document | Updated | Commit |
|----------|---------|--------|
| n/a — not user-visible (see proposal.md Documentation Impact) | — | — |

## Gate log

| Gate | Condition | Passed | Evidence |
|------|-----------|--------|----------|
| 1 | Issue + Specification + Acceptance Criteria | yes | Issue #1008; this `openspec/changes/fix-bruno-login-field-names/` |
| 2 | Failing tests written, test cases designed | yes (substituted) | See `tasks.md` §3 — live reproduction: broken payload → `token=null`, fixed payload → real JWT |
| 3 | Suite green, coverage held, docs updated | yes | Backend 1890/1890 unaffected; `mvn verify` BUILD SUCCESS; Bruno suite obtains a token and runs (local DB pollution unrelated, see `tasks.md` §6.4); `preflight.sh --fix` passed 16/16 |
| 4 | CI green, review approved, no conflicts | pending | — |
| 5 | Deployed, smoke test passed, Issue closed | pending | — |

## Exceptions

Gate 2's "write a failing test first" is not applicable in its literal form:
the defect is a hardcoded string in a GitHub Actions YAML step, which has no
unit/integration test harness. The equivalent verification — reproducing the
failure with the exact command CI runs, then confirming it succeeds after the
fix, plus a live CI run on the PR — is recorded in `tasks.md` and this
ledger's Requirement coverage table instead. This substitution is recorded
here per Constitution §12 rather than silently skipped.

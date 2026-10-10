# Traceability

> Governed by [CONSTITUTION.md](../../../../CONSTITUTION.md) — P4 Traceability.

## Chain

```text
Issue → Specification → Tasks → Commits → PR → Merge → Release
```

| Link | Reference | Status |
|------|-----------|--------|
| Issue | #1257 | open |
| Parent | #1197 Phase 0.2 | open |
| Related | #1419 (Pages/modules prep — merge prerequisite) | open / serialize |
| Use Case | CU76 | linked |
| Specification | `docs/openspec/changes/ci-1257-path-scoped-workflows/` | Gate 1 in progress |
| Branch | `cursor/ci-1257-path-scoped-cf98` | created |
| Tasks | `tasks.md` | ready |
| Pull Request | pending | — |

## Requirement coverage

| Scenario (Acceptance Criterion) | Verification | Status |
|---------------------------------|--------------|--------|
| Docs-only PR skips Java + Playwright leaves | path filters + aggregator | pending |
| Backend PR still runs CI + product E2E | filter `product` / `backend` | pending |
| `main` push runs full suite | `changes` force-true | pending |
| Aggregator names stable | invariant tests | pending |
| No `on.paths` starving main | invariant tests | pending |

## Permanent documentation updated

| Document | Updated | Commit |
|----------|---------|--------|
| CI-PREFLIGHT / REPO-SPLIT-PLAN / CONSTITUTION / CHANGELOG | pending | this PR |

## Gate log

| Gate | Condition | Passed | Evidence |
|------|-----------|--------|----------|
| 1 | Issue + Specification + AC | pending | this change |
| 2 | Invariant tests designed (red then green) | pending | |
| 3 | Workflows + docs | pending | |
| 4 | CI / PR + `check-heavy-ci.sh` | pending | |
| 5 | Optional docs-only smoke ≤3 min | pending | |

## Exceptions

- Issue label `in-progress`: integration token may 403 — sync via PR body + `Closes #1257` `Refs #1197`.
- Product Playwright: still required for product-path PRs; skipped only when `product` filter false.

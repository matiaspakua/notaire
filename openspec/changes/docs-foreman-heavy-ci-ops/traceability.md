# Traceability

> Governed by [CONSTITUTION.md](../../../CONSTITUTION.md) — P4 Traceability.
> This is the change's ledger. Rows below Tasks stay `pending` until the step
> actually happens — never pre-fill them.

## Chain

```text
Issue → Specification → Tasks → Commits → PR → Merge → Release
```

| Link | Reference | Status |
|------|-----------|--------|
| Issue | #1153 | open |
| Use Case | CU76 — Quality Assurance and Testing Infrastructure | exists |
| Specification | `openspec/changes/docs-foreman-heavy-ci-ops/` (`skip_specs: true`) | drafted |
| Branch | `cursor/docs-foreman-heavy-ci-69d3` | created |
| Tasks | `tasks.md` | in progress |
| Commits | `5a20839e` (foreman docs); OpenSpec commit pending | in progress |
| Pull Request | #1151 | open |
| CI run | pending — wait for heavy CI via `check-heavy-ci.sh 1151` | pending |
| Merge commit | pending | pending |
| Release / tag | n/a — docs-only, no release artifact | n/a |
| Smoke test | grep heavy script / wake-up / serialize / gh-401 in cloud-foreman.md | pending |

## Requirement coverage

n/a — `skip_specs: true`. Acceptance Criteria are in Issue #1153.

| Scenario (Acceptance Criterion) | Test | Status |
|---------------------------------|------|--------|
| Foreman states merge only after `check-heavy-ci.sh` exit 0 on current head | Grep heavy script in `.claude/agents/cloud-foreman.md` | passed |
| CI subscription “all checks success” is wake-up only | Grep wake-up / re-run heavy in foreman | passed |
| Playwright-heavy PRs serialized; Dependabot draft floods | Grep Serialize Playwright / Dependabot in foreman | passed |
| Coordinator owns merge when worker `gh` returns 401 | Grep 401 / coordinator in foreman | passed |
| Docs-only change; no product code | `git diff` scoped to agents + openspec | passed |
| OpenSpec Gate 1 complete (`skip_specs`) | `openspec validate --strict` + `validate-sdlc-plan.sh` | pending |
| PR commits include `Closes #1153` | Commit message inspection | pending |
| Does not use or depend on `local-ai/` | Grep change for runtime local-ai deps | passed |

## Permanent documentation updated

| Document | Updated | Commit |
|----------|---------|--------|
| `.claude/agents/cloud-foreman.md` | yes | `5a20839e` |
| `openspec/changes/docs-foreman-heavy-ci-ops/` | yes | pending |

## Gate log

| Gate | Condition | Passed | Evidence |
|------|-----------|--------|----------|
| 1 | Issue + Specification + Acceptance Criteria | yes | Issue #1153; this change folder |
| 2 | Failing tests written, test cases designed | n/a (docs; verification plan in design.md) | design.md — Testing Strategy |
| 3 | Suite green, coverage held, docs updated | yes (docs-only) | OpenSpec validators + grep |
| 4 | CI green, review approved, no conflicts | pending | `bash scripts/check-heavy-ci.sh 1151` |
| 5 | Deployed, smoke test passed, Issue closed | pending | — |

## Exceptions

- **TDD (Gate 2)**: Markdown / agent docs only; verification via grep + OpenSpec validators.
- **Branch naming**: Cloud Agent task required `cursor/docs-foreman-heavy-ci-69d3`; Constitution form recorded via Issue #1153.
- **in-progress label**: `gh issue edit` may be denied for this integration.
- **sdlc-exception**: not used — OpenSpec change folder satisfies Process Checks.

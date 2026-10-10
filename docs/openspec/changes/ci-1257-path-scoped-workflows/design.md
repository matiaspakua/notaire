# Design — path-scoped CI (#1257)

## Context

Phase 0.2 of [#1197](https://github.com/matiaspakua/notaire/issues/1197) / ADR-024.
Docs, OpenSpec, and agent-only PRs still run the full Java + Playwright stack (~12 min)
even when no product code changed. Suite aggregators today treat any non-`success` result
(including intentional `skipped`) as failure, which would break path filters if added
naively. Issue [#1257](https://github.com/matiaspakua/notaire/issues/1257) (CU76).

## Goals / Non-Goals

**Goals**

- Classify PR paths and skip irrelevant heavy leaf jobs.
- Keep required check *names* (`CI`, `Frontend CI`, `Playwright E2E`, …) green via
  aggregators that accept intentional `skipped`.
- Always run the full suite on `push` to `main`, `workflow_dispatch`, and `schedule`.
- Guard the layout with `workspace/tests/test_ci_workflow_invariants.py`.

**Non-Goals**

- Playwright sharding (#1258), agent context trim (#1259), OpenAPI→TS types (#1260).
- Workflow-level `on.paths` that omit entire workflows.
- Changing branch-protection required check names.
- Product application code.

## Decisions

| Decision | Choice | Alternative considered | Why the alternative lost |
|----------|--------|------------------------|--------------------------|
| Filter placement | Job-level `dorny/paths-filter` + always-run `changes` | Workflow `on.paths` | Omitting the workflow leaves required checks pending on `main` |
| Aggregator policy | Accept `success\|skipped` | Keep success-only | Path skips would fail suite aggregators |
| Playwright on backend-only | Run (`product` includes backend) | Skip E2E for backend-only | API changes still need Bruno/E2E; conservative |
| OpenAPI | Run when `openapi\|backend\|ci` | Always run | Unnecessary Maven export on docs-only PRs |
| Non-PR events | Force all filter outputs `true` | Skip `changes` job on non-PR | Skip cascade would starve `main` leaf jobs |

## Riesgos / Trade-offs

| Risk | Mitigation |
|------|------------|
| Filter false-negative skips a needed suite | `ci` filter forces full suites when workflows/guards change; product filter is conservative |
| Aggregator greens a cancelled/failed job | Only `success` and `skipped` accepted; `failure`/`cancelled` still fail |
| CONSTITUTION conflict with open #1419 | Path-scoped bullet is additive; rebase/merge after serialize lands #1419 |
| Branch protection expects OpenAPI check always | Added `suite-openapi` aggregator named `OpenAPI Contract` accepting skipped |

## Testing Strategy

| Spec scenario | Level | Verification |
|---------------|-------|--------------|
| Changes job present on four workflows | Guard unit | `PathScopedCiInvariantsTest.test_path_filter_workflows_have_changes_job` |
| Docs-only skips Java + Playwright leaves | Guard + CI evidence | Job `if:` on `build` / `backend-build`; optional post-merge docs-only PR |
| Backend PR runs CI + product E2E | Guard | `test_ci_build_gated_on_backend_or_ci_filter`, `test_playwright_roots_gated_on_product_filter` |
| Main always full suite | Guard + design | `changes` `out` step forces true when `event_name != pull_request` |
| Aggregators accept skipped | Guard | `test_suite_aggregators_accept_skipped` |
| No `on.paths` starving main | Guard | `test_workflows_do_not_use_on_paths_that_starve_main` |
| Aggregator names stable | Guard | `test_suite_aggregator_names_unchanged` |

TDD: path-scoped invariants observed failing on current workflows, then patches applied to green.

## Regression Strategy

- `python3 workspace/tests/test_ci_workflow_invariants.py` (existing page-deploy and playwright invariants retained).
- This PR changes workflows (`ci` filter true) → full heavy CI still runs; `bash workspace/sdlc/check-heavy-ci.sh <pr>` before ready.

## Playwright Strategy

No new product UI. Path filters only change *when* existing Playwright / Bruno jobs run.
Docs-only / agent-only PRs skip `backend-build` / `frontend-build` / `api-tests` / `e2e-tests`;
product PRs keep the suite unchanged. This PR itself touches workflows so Playwright runs.

## Deployment Strategy

No runtime deploy. Workflow YAML takes effect on the next Actions run after merge to `main`.
`deploy-github-page.yml` unchanged; still gates on CI success on `main` (full suite on main push).

## Rollback Strategy

1. Revert the #1257 PR on `main` — restores always-on leaf jobs.
2. If aggregators mis-green a failed suite, revert immediately; required check names unchanged.
3. Hotfix: force all `changes` outputs `true` in the `out` step while investigating filter bugs.

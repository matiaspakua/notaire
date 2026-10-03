> Governed by [CONSTITUTION.md](../../../CONSTITUTION.md).

## Context

On #1145 / #1147, rapid fix pushes left superseded `ci.yml` / Playwright runs
(`cancel-in-progress: false`) holding runners while the new head stayed pending.
Agent `gh` cannot cancel Actions (403).

## Goals / Non-Goals

**Goals:** Latest push on a PR branch cancels in-progress CI and Playwright runs
on that same ref.

**Non-Goals:** Cross-PR cancellation; pages deploy mid-cancel; runner scaling.

## Decisions

| Decision | Choice | Why |
|----------|--------|-----|
| Cancel scope | Same GitHub ref only (`github.ref` / workflow+ref groups) | Avoids cancelling unrelated PRs |
| Workflows flipped | `ci.yml`, `playwright-e2e.yml` | These are the heavy runner consumers |
| Pages deploy | Keep `cancel-in-progress: false` | Deploy must finish atomically |
| Verification | Static YAML unit/script assert | Workflows are config; TDD via failing flag assert |

## Riesgos / Trade-offs

| Risk | Mitigation |
|------|------------|
| Mid-suite cancel loses signal on intermediate SHA | Acceptable — only latest head is merge authority (`check-heavy-ci.sh`) |
| Accidental cross-PR cancel | Concurrency groups keyed by ref / workflow+ref |
| Docs drift vs YAML | Unit assert + CI-MERGE-GATE note |

## Testing Strategy

- Mechanical: assert YAML concurrency blocks set `cancel-in-progress: true` for
  `ci.yml` and `playwright-e2e.yml`; `deploy-github-page.yml` remains false.
- No product UI change — Playwright suite still runs as regression on the PR.

## Regression Strategy

Full heavy gate on the PR (`bash scripts/check-heavy-ci.sh <pr>`).

## Playwright Strategy

No new E2E scenarios; existing suite remains the UI gate for the workflow PR.

## Deployment Strategy

Merge via PR to `main`. Workflows take effect on subsequent pushes. No app deploy.

## Rollback Strategy

Revert the two boolean flips (and the unit assert / docs note). No DB / app rollback.

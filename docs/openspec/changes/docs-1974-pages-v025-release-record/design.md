# Design — Pages release record for v0.2.5

> Governed by [CONSTITUTION.md](../../../../../CONSTITUTION.md).

## Context

`v0.2.5` was cut by release-please (#1969). Wave 9 product work landed before that tag; Pages still
served `releases.json` with `productVersion: 0.2.4` until regenerated. CHANGELOG still listed Wave 9
under `[Unreleased]`.

## Goals / Non-Goals

**Goals:** `releases.json` leads with 0.2.5; CHANGELOG attributes Wave 9 to `[0.2.5]`.
**Non-Goals:** New SemVer cut; Pages UX changes; runtime product behaviour.

## Decisions

1. Regenerate via `python3 workspace/product_release.py --write-pages` (canonical writer).
2. Move only Wave 9 / #1963-cluster bullets into `[0.2.5]`; leave #1971 (and later) Unreleased.

## Riesgos / Trade-offs

| Risk | Mitigation |
|------|------------|
| Stale Pages cache after deploy | Hard refresh / next Actions deploy |

## Testing Strategy

Inspect JSON; CHANGELOG review; Pages CI if path-scoped; smoke Releases page after deploy.

## Regression Strategy

n/a — data/docs only.

## Playwright Strategy

n/a — no UI behaviour change beyond version string data.

## Deployment Strategy

Merge to `main`; `Deploy GitHub Page` publishes the new `releases.json`.

## Rollback Strategy

Revert the PR.

> Governed by [CONSTITUTION.md](../../../../CONSTITUTION.md) — §6 Quality Gates.

## Context

`generate-factory-metrics.py`, `generate-repolens-bench.py`, `generate-ai-sdlc-history.py` and the bench runners each refresh one artifact; the session-start hook already reports OpenSpec status and unpushed branches.

## Goals / Non-Goals

**Goals:** One command, one check, visible at session start.
**Non-Goals:** Network calls at session start (the check reads the local `origin/main`).

## Decisions

Python stdlib script in `workspace/ci` (Notaire adopter wiring; the kit and RepoLens stay adopter-free). Staleness compares versions and git ancestry, not timestamps: timestamps follow data (#2429). The hook runs `--check --brief`, which skips anything slow.

## Riesgos / Trade-offs

A slow session start: the brief check only reads files and runs two `git rev-list` calls.

## Testing Strategy

Red first: `workspace/tests/test_refresh_factory_evidence.py` (step list, opt-ins, version and ancestry staleness in a temp repo) and a RepoLens test for the version stamp.

## Regression Strategy

Existing generator tests; the session hook still exits 0.

## Playwright Strategy

n/a — no UI surface.

## Deployment Strategy

Ships with the next release.

## Rollback Strategy

Revert the PR.

## Migration Plan

None.

## Open Questions

None.

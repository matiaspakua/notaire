> Governed by [CONSTITUTION.md](../../../../CONSTITUTION.md).

## Context

Dead `mobile` project config; real specs are TS-0109 and TS-0153.

## Goals / Non-Goals

**Goals:** Real iPhone SE runs; zero-match projects fail CI.
**Non-Goals:** New mobile scenarios; Android project.

## Decisions

1. Match `**/*mobile*.spec.ts`; ignore same pattern on chromium.
2. Guard via `playwright test --list --reporter=list` counts per project.

## Riesgos / Trade-offs

- [Risk] CI time + shard matrix → Mitigation: only two specs today.

## Testing Strategy

`test_playwright_projects_nonempty.py` (static + `--list`).

## Regression Strategy

Existing E2E CI; Process Checks discover testing/tests.

## Playwright Strategy

Config-only; mobile project is the E2E surface.

## Deployment Strategy

Next merge.

## Rollback Strategy

Revert PR.

## Migration Plan

None.

## Open Questions

None.

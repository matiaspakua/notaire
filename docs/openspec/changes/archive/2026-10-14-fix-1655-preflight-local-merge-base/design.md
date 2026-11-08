> Governed by [CONSTITUTION.md](../../../../CONSTITUTION.md) — §6 Quality Gates.

## Context

The three-dot form base...HEAD already means merge-base(base, HEAD)..HEAD, so removing the override locally is enough.

## Goals / Non-Goals

**Goals:** no PREFLIGHT_SKIP needed after merging main.
**Non-Goals:** a new env var; GITHUB_ACTIONS is already set by Actions.

## Decisions

1. **Gate on GITHUB_ACTIONS, not on the shape of the commit.** The shape is ambiguous locally; the environment is not.

## Riesgos / Trade-offs

- [A non-Actions CI runner] → none configured; the ci.yml and sdlc-process.yml run on Actions.

## Testing Strategy

workspace/tests/test_pr_checks.py and test_openspec_archive_hygiene.py cover each scenario in a temp repo.

## Regression Strategy

python3 -m unittest discover -s workspace/tests

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

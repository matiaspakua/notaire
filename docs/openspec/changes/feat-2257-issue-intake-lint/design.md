> Governed by [CONSTITUTION.md](../../../../CONSTITUTION.md) — §6 Quality Gates.

## Context

The kit reaches the tracker only through profile commands (`[tracker] verify`). Intake needs the issue text, so the profile gains a `fetch` command, expanded with `{issue}`, that prints one JSON object with `title`, `body` and `labels` (strings or objects with `name`).

## Goals / Non-Goals

**Goals:** catch under-specified issues before an agent starts; keep skip distinct from pass.
**Non-Goals:** judging the quality of prose; auto-fixing issues.

## Decisions

1. **Pure `lint()` core** taking the issue dict and a link resolver, so tests need no network.
2. **Section match is case-insensitive prefix** on `##` headings (`Test plan (RED/PRESERVE)` satisfies `Test plan`).
3. **Exit 3 for no-evaluable**, distinct from fail, so a gate cannot treat it as pass.
4. **Advisory in the Notaire profile** until a clean report over the open issues exists.

## Riesgos / Trade-offs

- [Fetch output shape differs per tracker] → documented minimal shape; labels accept strings or `{name}`.
- [Many `#N` links slow the check] → `intake.max_links` cap with a deterministic order.

## Testing Strategy

`tests/test_intake.py`: pure lint cases and CLI cases with `--file` fixtures.

## Regression Strategy

`framework/ai-sdlc/verify.sh` (all kit tests, independence test) and `aisdlc config validate` on the Notaire profile.

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

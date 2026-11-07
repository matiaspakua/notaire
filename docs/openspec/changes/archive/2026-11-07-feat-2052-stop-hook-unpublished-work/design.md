> Governed by [CONSTITUTION.md](../../../../CONSTITUTION.md) — §6 Quality Gates.

## Context

Claude Code documents that a Stop hook exiting 2 blocks the stop with stderr as the reason, and that `stop_hook_active` is set when a stop hook already kept the agent going. `.claude/rules/hooks.md` warns against noisy hooks.

## Goals / Non-Goals

**Goals:** enforce #1681 at the moment an agent would walk away from local-only work.
**Non-Goals:** judging uncommitted edits; other agent runtimes.

## Decisions

- Fire only on Issue branches and only for commits on no remote.
- Block at most once per stop.
- Fail open on any error.

## Riesgos / Trade-offs

- A session told not to push is blocked once: it explains why in its final message, and the retry passes.

## Testing Strategy

`workspace/tests/test_stop_hook_publication.py`: six tests with temporary repositories and synthetic payloads; the settings test observed failing before registration.

## Regression Strategy

The full kit suite (`python3 -m unittest discover -s framework/ai-sdlc/tests`) and `workspace/tests` run in `preflight.sh`; existing commands keep their outputs (`report` reads chained records).

## Playwright Strategy

n/a — no UI surface.

## Deployment Strategy

Ships with the next release; no runtime component.

## Rollback Strategy

Revert the PR.

## Migration Plan

None.

## Open Questions

None.

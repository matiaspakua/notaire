> Governed by [CONSTITUTION.md](../../../../CONSTITUTION.md) — §6 Quality Gates.

## Context

Adoption of PR 1's kit commands in Notaire's profile, policy and templates; the assessment document records the comparison with agentic-os, gentle-ai and factory-control and a web challenge of each decision.

## Goals / Non-Goals

**Goals:** turn the Foreman merge rule's Owner-only clause into an exit code; give agents a written protocol for uncertainty.
**Non-Goals:** enforcing `scope` and `size` (advisory first); changing the fleet's commit authority (B2).

## Decisions

- `protected` and `evidence verify` are required at `merge`; `scope` and `size` advisory at `pre-pr`.
- The protocol lives in the operational rule; the Constitution carries one bullet and points to it.
- Merges after PR 1: its gates call PR 1's commands.

## Riesgos / Trade-offs

- Merging before PR 1 makes `gate merge` fail on unknown commands; the PR stays draft until PR 1 merges.

## Testing Strategy

`workspace/tests/test_aisdlc_profile_adoption.py` (8 tests, 7 observed failing on `main`) and two new scaffold tests.

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

> Governed by [CONSTITUTION.md](../../../../CONSTITUTION.md) — §6 Quality Gates,
> §7 Testing Rules, §11 Release Rules.

## Context

`runs.start` and `intent_model.attach_run` exist; only the CLI flag is missing.

## Goals / Non-Goals

Goal: CLI parity with the API.

## Decisions

Reuse `intent_model.show_intent` for validation and `attach_run` after the run starts, like `serve.py`.

## Riesgos / Trade-offs

None.

## Testing Strategy

CLI test in a throwaway repo: intent recorded and attached, status reports it, unknown intent refused.

## Regression Strategy

Full kit suite.

## Playwright Strategy

n/a — no UI.

## Deployment Strategy

Ships with the kit.

## Rollback Strategy

Revert the merge.

## Migration Plan

None.

## Open Questions

None.

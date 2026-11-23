> Governed by [CONSTITUTION.md](../../../../CONSTITUTION.md) — §6 Quality Gates.

## Context

runs.derive_status merges step events into run steps; ProductionLine Detail shows catalog inputs/outputs only.

## Goals / Non-Goals

**Goals:** Inspect every stage's real inputs and outputs from the dashboard.
**Non-Goals:** Storing file contents or logs in the ledger; a write API for events over HTTP.

## Decisions

New module aisdlc/step_io.py validates one shape for both sides; records stored on the ledger event so the chain covers them; step entries merge per side. Dashboard derives file links as <repo>/blob/<ref>/<path> (ref = first output commit, else HEAD).

## Riesgos / Trade-offs

Prompts are stored in the evidence ledger: the docs say no secrets; size limits bound growth. Contract change is additive (OpenAPI 1.7.0).

## Testing Strategy

Kit unittest (CLI + validation), dashboard Vitest for link derivation, Playwright with axe.

## Regression Strategy

Existing runs without the fields render; existing harness report tests pass.

## Playwright Strategy

framework/ai-sdlc/factory-dashboard/e2e/step-io.spec.ts

## Deployment Strategy

Ships with the next release.

## Rollback Strategy

Revert the PR.

## Migration Plan

None.

## Open Questions

None.

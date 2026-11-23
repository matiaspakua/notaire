> Governed by [CONSTITUTION.md](../../../../CONSTITUTION.md) — §6 Quality Gates.

## Context

Ledger has run and step events; cost.jsonl rows lack run; Junie logs hold llm_cost_trajectory_snapshot; transcripts hold message.usage; .repolens/usage.jsonl logs calls; bench results hold baseline vs graph summaries.

## Goals / Non-Goals

**Goals:** Answer the three questions per intent and overall with real data.
**Non-Goals:** Estimating per-intent RepoLens savings without a paired run.

## Decisions

Attribution by time window from the ledger; Foreman usage attributed only when a window is given or the run window is dedicated; prices per million tokens with cache_write at the 5-minute rate unless the 1-hour split is known (2x input).

## Riesgos / Trade-offs

Attribution overlaps when work runs in parallel: windows are explicit and reported. Prompts are never imported, only counts.

## Testing Strategy

Kit unit and CLI tests with fixture logs and transcripts; dashboard unit and Playwright; Pages vitest.

## Regression Strategy

Existing cost, report and telemetry tests.

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

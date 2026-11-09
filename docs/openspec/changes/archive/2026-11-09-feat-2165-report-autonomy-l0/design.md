> Governed by [CONSTITUTION.md](../../../../CONSTITUTION.md) — §6 Quality Gates.

## Context

Winner of Isolator/ForgePort challenge: consume Wilson L0 in report surfaces.

## Goals / Non-Goals

**Goals:** One autonomy object in summarize → JSON/MD/HTML.
**Non-Goals:** New CLI; portal; Isolator.

## Decisions

Call `improve.autonomy_from_runs` from `evidence.summarize` (same ledger read). HTML
renders status/level/wilson_lower/trials only — no friction text that could look like prompts.

## Riesgos / Trade-offs

Circular import risk: evidence → improve. Prefer lazy import inside summarize or move
autonomy_from_runs call in report() after summarize. Safest: add autonomy in `report()`
after summarize to avoid evidence↔improve cycles if evidence imported by improve.

Actually improve already imports evidence._read. So evidence importing improve would cycle.
**Decision:** attach autonomy in `report()` (and any other callers that need it), OR put a
thin helper in improve and call from report() only. summarize stays pure of improve;
`report()` merges autonomy.

Alternatively extract wilson counting to a tiny module — overkill. Use report() merge.

## Testing Strategy

Extend ReportHtmlTest + unit on report --json autonomy key.

## Regression Strategy

Existing HTML tests; AutonomyWilsonTest unchanged.

## Playwright Strategy

n/a

## Deployment Strategy

Kit only.

## Rollback Strategy

Revert PR.

## Migration Plan

None.

## Open Questions

None.

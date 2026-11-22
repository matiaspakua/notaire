> Governed by [CONSTITUTION.md](../../../../CONSTITUTION.md) — §6 Quality Gates,
> §7 Testing Rules, §11 Release Rules.

## Context

The Overview stacks one card per stage. The kit already exposes everything a diagram needs: `GET /factory/line` (stages with gates, agents, skills, model, effort, grants), `/status` (run, current stage and step, previous and next steps, intent), `/report` gates[] (last status, pass rate, median per gate), `/telemetry?group=stage` (runs, time, cost, failures) and `/events` (live). The visual reference is the factory deck's workflow slide; the Owner limited its style to this diagram.

## Goals / Non-Goals

Goals: one glance answers where the run is, what each station is, which gates hold, what it cost and how far the intent has come. Non-goals: restyling other screens; a kit-side station kind; per-run gate results.

## Decisions

- D1 Pure model in `src/ui/line-model.ts` (stationKind, stationStates, gateState, stationThroughput, intentProgress), unit-tested without React; the view only renders.
- D2 Kind: agent when a stage lists agents, human for `merge` (the Foreman or Owner merge decision), deterministic otherwise. A follow-up issue asks the kit for a `kind` field.
- D3 Gate state from the latest report entry per stage and gate; skip and todo are "not evaluable", no entry is "not run".
- D4 Throughput sums telemetry rows keyed by the stage or by a workflow step whose stage is that station.
- D5 Deck shapes on dashboard tokens: square stations with a 1.5px `--text` border, a 2.5px rail, kind markers (circle, square, diamond) and `--line-agent`/`--line-human`/`--line-det` scoped to `.pl`, with dark values. Platform font, no new dependency.
- D6 Motion explains state only: the token glides to the new station on change and the running lamp pulses; no looping flow; reduced motion removes both.
- D7 Under 768px the rail is vertical; the line's own region scrolls sideways when stations exceed the width, never the page.

## Riesgos / Trade-offs

The kind heuristic can mislabel a stage until the kit declares kinds. Report gates are aggregate, not per run. Telemetry keyed by step ids depends on the workflow mapping; unmapped rows are not shown on any station.

## Testing Strategy

Vitest for every model function (states for idle, running, failed and succeeded runs; skip never passed; step rows summed into stations; cost null versus zero; progress counts). Playwright against the seeded kit for the diagram, keyboard selection, gates, loop, strip, 320px and axe in both themes.

## Regression Strategy

Existing Overview e2e assertions on stations (`data-testid="station"`, gate pills, station details) are updated to the diagram without weakening; capabilities cards stay.

## Playwright Strategy

`framework/ai-sdlc/factory-dashboard/tests/e2e/dashboard.spec.ts`: diagram stations in order, running token station, gate states including not evaluable, correction loop label, detail panel by keyboard, intent strip text, no page overflow at 320px, axe light and dark.

## Deployment Strategy

Ships with the kit; the dashboard is rebuilt and restarted locally (`pnpm build && pnpm start`).

## Rollback Strategy

Revert the merge commit; the previous Overview cards return. The workflow version stays harmless (older runs keep their pinned version).

## Migration Plan

None.

## Open Questions

None.

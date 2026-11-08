> Governed by [CONSTITUTION.md](../../../../CONSTITUTION.md) — §6 Quality Gates.

## Context

V1 assessment (control plane + ports), thin factory plan, Port.io contrast (adapt/skip), landed #1958/#2052/#2121–#2124.

## Goals / Non-Goals

**Goals:** One durable in-repo roadmap + Gate 1 + a mechanical test that the required topics are present.
**Non-Goals:** Implementing ports, HTTP UI, or TelemetryPort. Cloning Port.io. Mega-PR of V1 F-01…F-26.

## Decisions

New FACTORY-ROADMAP.md keeps FACTORY-FOUNDATION.md as the landed-slice note. Milestones are one Issue/PR each where practical. Validate every slice on Notaire.

## Riesgos / Trade-offs

Roadmap could sprawl into a second Constitution — keep KISS tables and link ADRs/issues instead of restating policy.

## Testing Strategy

workspace/tests/test_factory_roadmap.py (red then green).

## Regression Strategy

Existing aisdlc adoption and foundation docs tests stay green.

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

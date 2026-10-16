> Governed by [CONSTITUTION.md](../../../../CONSTITUTION.md) — §6 Quality Gates.

## Context

Epic #1693 ArchUnit R1 shrink; after Concept #1815.

## Goals / Non-Goals

**Goals:** Clear NotebookController R1 edge.
**Non-Goals:** ReportService hexagon; domain Notebook extraction.

## Decisions

Keep ReportService on controller for cover PDF (no R1 edge). NotebookResponse mapping from NotebookView.

## Riesgos / Trade-offs

Create validation/not-found map bodies must remain.

## Testing Strategy

HexagonArchitectureTest; existing notebook tests.

## Regression Strategy

Bruno notebooks; cover PDF integration if any.

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

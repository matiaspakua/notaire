> Governed by [CONSTITUTION.md](../../../../CONSTITUTION.md) — §6 Quality Gates.

## Context

Epic #1693 ArchUnit R1 shrink; after Person #1813.

## Goals / Non-Goals

**Goals:** Clear ConceptController R1 edges.
**Non-Goals:** Domain Concept extraction; FE concepts already hexagonized.

## Decisions

In-use conflict bodies stay Map shapes; Failed results map via ErrorResponses.

## Riesgos / Trade-offs

Update/delete in-use conflict message strings must remain.

## Testing Strategy

ConceptControllerTest; HexagonArchitectureTest.

## Regression Strategy

Bruno concepts; CoreBusinessControllersIntegrationTest Concept nested.

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

> Governed by [CONSTITUTION.md](../../../../CONSTITUTION.md) — §6 Quality Gates.

## Context

Notaire factory validation; anchors #2117/#1418 process gaps.

## Goals / Non-Goals

**Goals:** Make catalog list useful on Notaire without seeding.
**Non-Goals:** Writing catalog JSON automatically.

## Decisions

Overlay discover; JSON wins on id collision; omit CHANGELOG while #2150 fence held.

## Riesgos / Trade-offs

Large skills trees slow list — acceptable for Notaire scale.

## Testing Strategy

CatalogSkillsDiscoverTest + CatalogNotaireSmokeTest

## Regression Strategy

CatalogCrudTest

## Playwright Strategy

n/a

## Deployment Strategy

Ships with the next release.

## Rollback Strategy

Revert the PR.

## Migration Plan

None.

## Open Questions

None.

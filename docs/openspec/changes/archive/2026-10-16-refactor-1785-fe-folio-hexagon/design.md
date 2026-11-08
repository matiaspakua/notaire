> Governed by [CONSTITUTION.md](../../../../CONSTITUTION.md) — §6 Quality Gates.

## Context

Child of #1693. Folios admin page mixes folio-type catalog and folio instance CRUD in one screen.

## Goals / Non-Goals

**Goals:** Clear the folios page api-client suppression without changing REST contracts or UI behaviour.
**Non-Goals:** Extracting protocol/reports folio-type reads; CHANGELOG; backend ports.

## Decisions

Two cores (folio-type + folio) matching the two resource families on the page; notaries loaded via folio gateway.

## Riesgos / Trade-offs

Optimistic-lock version on folio-type update (#655) must keep passing through the gateway.

## Testing Strategy

Vitest: use-case fakes + gateway DTO mapping; architecture-boundaries pin.

## Regression Strategy

Existing folios-admin-page unit test and Playwright admin folios flows.

## Playwright Strategy

n/a — no UI behaviour change; existing admin folios E2E covers the screen

## Deployment Strategy

Ships with the next release.

## Rollback Strategy

Revert the PR.

## Migration Plan

None.

## Open Questions

None.

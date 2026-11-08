# Management R2 shrink — ports/views (no entity on ports)

> Governed by [CONSTITUTION.md](../../../../CONSTITUTION.md). This proposal documents
> **only this change**; permanent documentation remains the single source of truth.

| Field | Value |
|-------|-------|
| GitHub Issue | #1850 |
| Use Case | CU14 / CU09 / gestión lifecycle (architectural refactor under epic #1693 / ADR-030) — no behaviour change |
| Branch | `cursor/1850-management-r2-a603` |
| Gate 1 status | passed |

## Objetivo

ArchUnit R2 still flagged the management application slice (~56 edges): entity-speaking CU83 ports and `Management*Service` helpers spoke `business.*` / Spring Data repositories. Controllers also injected some helpers directly (R5 risk once helpers move outbound).

## What Changes

- Convert CU83 outbound ports to domain views / IDs (reuse `domain.management.ManagementStatus`; add management transition snapshot view).
- Move entity-bound `Management*Service` helpers (+ write-guard) to `adapter.out.persistence.management`.
- Extend `ManagementOpsPort` / inbound query-manage ports for case-summary, financial summary, and pending balance so HTTP adapters stay ports-only.
- Replace `ExternalEntityDocumentService` → `ManagementTransitionService` with a non-entity outbound port.
- Shrink `hexagon-baseline.txt` (~56 R2 edges). Omit CHANGELOG while #1766 holds Unreleased.

## Reglas de negocio

| Rule | Source | New / Changed / Made explicit |
|------|--------|-------------------------------|
| Management HTTP adapters call inbound ports only; entity/repository access stays in outbound persistence adapters | #1850 / ADR-030 / epic #1693 | New |

## Capabilities

### New Capabilities
- `management-r2-ports`: Management application layer free of entity/repository R2 edges; controllers use inbound ports only

### Modified Capabilities

## Impact Analysis

### Módulos afectados

| Module | Touched | What changes |
|--------|---------|--------------|
| `backend-api` | yes | domain views; port signatures; move helpers; inbound methods; baseline |
| `frontend` | no | — |

### Surface area

- Entities / endpoints / Flyway / config / deps: none (same management REST)

### Architecture review

ADR-030 slice log only.

## Documentation Impact

| Permanent document | What must change |
|--------------------|------------------|
| ADR-030 | slice log for management R2 |
| CHANGELOG.md | n/a — #1766 |

## Out of Scope

Workflow R2 (#1851); CHANGELOG Unreleased; behaviour changes to CU83 transitions.

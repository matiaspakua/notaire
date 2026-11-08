# Workflow R2 shrink — ports/views (no entity on ports)

> Governed by [CONSTITUTION.md](../../../../CONSTITUTION.md). This proposal documents
> **only this change**; permanent documentation remains the single source of truth.

| Field | Value |
|-------|-------|
| GitHub Issue | #1851 |
| Use Case | CU23 / workflow catalog + trace (architectural refactor under epic #1693 / ADR-030) — no behaviour change |
| Branch | `cursor/1851-workflow-r2-a603` |
| Gate 1 status | passed |

## Objetivo

ArchUnit R2 still flagged the workflow application slice (~30 edges): dead entity-speaking `Workflow*RepositoryPort` types and entity-bound `WorkflowTraceService` / `DocumentReentryService`.

## What Changes

- Delete unused `Workflow*RepositoryPort` + legacy persistence adapters (superseded by `*CatalogPort` from #1728–#1730).
- Move `WorkflowTraceService` / `DocumentReentryService` to `adapter.out.persistence.workflow`.
- Add inbound `QueryWorkflowTraceUseCase` / `DocumentReentryUseCase` (DTO-only) so `ManagementController` stays R5-clean.
- Shrink `hexagon-baseline.txt` (~30 R2 edges). Omit CHANGELOG while #1766 holds Unreleased.

## Reglas de negocio

| Rule | Source | New / Changed / Made explicit |
|------|--------|-------------------------------|
| Workflow HTTP callers use inbound ports only; entity/repository access stays in outbound workflow adapters | #1851 / ADR-030 / epic #1693 | New |

## Capabilities

### New Capabilities
- `workflow-r2-ports`: Workflow application layer free of entity/repository R2 edges; controllers use inbound ports for trace/reentry

### Modified Capabilities

## Impact Analysis

### Módulos afectados

| Module | Touched | What changes |
|--------|---------|--------------|
| `backend-api` | yes | delete dead ports; move helpers; inbound ports; baseline |
| `frontend` | no | — |

### Surface area

- Entities / endpoints / Flyway / config / deps: none (same management REST for trace/reentry)

### Architecture review

ADR-030 slice log only.

## Documentation Impact

| Permanent document | What must change |
|--------------------|------------------|
| ADR-030 | slice log for workflow R2 |
| CHANGELOG.md | n/a — #1766 |

## Out of Scope

Other R2 domains; AI SDLC docs (#1859); CHANGELOG Unreleased.

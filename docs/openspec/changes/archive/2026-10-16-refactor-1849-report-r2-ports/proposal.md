# Report R2 shrink — ports/views (no entity on ports)

> Governed by [CONSTITUTION.md](../../../../CONSTITUTION.md). This proposal documents
> **only this change**; permanent documentation remains the single source of truth.

| Field | Value |
|-------|-------|
| GitHub Issue | #1849 |
| Use Case | CU24 / CU25 / CU15 / CU80 – PDF reports (architectural refactor under epic #1693 / ADR-030) — no behaviour change |
| Branch | `cursor/1849-report-r2-a603` |
| Gate 1 status | passed |

## Objetivo

ArchUnit R2 still flagged the report application slice (~56 edges): builders and `ReportService` spoke `business.*` / Spring Data repositories, and a dead `DocumentCostTemplateRepositoryPort` exposed entities.

## What Changes

- Move `ReportService`, `ReportDocumentFactory`, `IndexBookReportBuilder`, `MonthlySwornStatementReportBuilder` to `adapter.out.report`.
- Add inbound `GenerateReportsUseCase` (PDF `byte[]` only) so inbound web adapters never depend on outbound adapters (R5).
- Delete unused entity-speaking `DocumentCostTemplateRepositoryPort` + persistence adapter.
- Shrink `hexagon-baseline.txt` (~56 R2 edges). Omit CHANGELOG while #1766 holds Unreleased.

## Reglas de negocio

| Rule | Source | New / Changed / Made explicit |
|------|--------|-------------------------------|
| Report HTTP adapters call inbound ports only; entity/repository access stays in outbound report adapters | #1849 / ADR-030 / epic #1693 | New |

## Capabilities

### New Capabilities
- `report-r2-ports`: Report application layer free of entity/repository R2 edges; controllers use `GenerateReportsUseCase`

### Modified Capabilities

## Impact Analysis

### Módulos afectados

| Module | Touched | What changes |
|--------|---------|--------------|
| `backend-api` | yes | move report builders; inbound port; delete dead cost-template port; baseline |
| `frontend` | no | — |

### Surface area

- Entities / endpoints / Flyway / config / deps: none (same PDF REST)

### Architecture review

ADR-030 slice log only.

## Documentation Impact

| Permanent document | What must change |
|--------------------|------------------|
| ADR-030 | slice log for report R2 |
| CHANGELOG.md | n/a — #1766 |

## Out of Scope

Management/workflow R2 (#1850/#1851); Jasper reintroduction; CHANGELOG Unreleased.

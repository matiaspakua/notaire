# Auxiliary protocol mixed controller ports-only

> Governed by [CONSTITUTION.md](../../../../CONSTITUTION.md). This proposal documents
> **only this change**; permanent documentation remains the single source of truth.

| Field | Value |
|-------|-------|
| GitHub Issue | #1825 |
| Use Case | CU81 – Gestión de Trámites en Protocolo Auxiliar (architectural refactor under epic #1693 / ADR-030) — no behaviour change |
| Branch | `cursor/1825-auxiliary-protocol-a603` |
| Gate 1 status | passed |

## Objetivo

`AuxiliaryProtocolController` still imports and returns `business.Deed` and calls `AuxiliaryProtocolService` directly.

## What Changes

Add Query/Manage inbound ports + `AuxiliaryProtocolOpsPort` adapter; thin controller; wire deed response from views. Omit CHANGELOG while #1766 holds Unreleased.

## Reglas de negocio

| Rule | Source | New / Changed / Made explicit |
|------|--------|-------------------------------|
| AuxiliaryProtocolController calls inbound ports only; entity access stays in the outbound adapter | #1825 / ADR-030 / epic #1693 / CU81 | New |

## Capabilities

### New Capabilities
- `auxiliary-protocol-hexagon`: Auxiliary-protocol REST behind inbound ports and outbound OpsPort

### Modified Capabilities

## Impact Analysis

### Módulos afectados

| Module | Touched | What changes |
|--------|---------|--------------|
| `backend-api` | yes | AuxiliaryProtocolController ports-only; OpsPort + adapter; use cases |
| `frontend` | no | — |
| `frontend-swing` | no | — |
| `infra` / observability | no | — |
| CI/CD (`.github/workflows`) | no | — |

### Surface area

- Entities: none
- Endpoints: none (same `/api/v1/auxiliary-protocol`)
- Database (Flyway `V{n}`): none
- Configuration / `.env`: none
- Dependencies: none

### Architecture review

Follows the existing architecture; ADR-030 slice log only.

## Documentation Impact

| Permanent document | What must change |
|--------------------|------------------|
| `docs/200-architecture/202-ADR/ADR-030-hexagonal-architecture-full-adoption.md` | slice log for AuxiliaryProtocol ports-only |
| `CHANGELOG.md` | n/a — omit while #1766 holds Unreleased |

## Out of Scope

ItemResponse/*Ref R1 (#1826); FE budgets (#1827); CHANGELOG Unreleased (#1766).

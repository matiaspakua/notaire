# Audit and standardize end-to-end project documentation

> Governed by [CONSTITUTION.md](../../../CONSTITUTION.md). This proposal documents
> **only this change**; permanent documentation remains the single source of truth.

| Field | Value |
|-------|-------|
| GitHub Issue | #921 |
| Use Case | CU76 – Quality Assurance and Testing Infrastructure; CU77 – Monitoreo de Operaciones y Gestión de Incidentes |
| Branch | `docs/921_documentation_audit` |
| Gate 1 status | draft |

## Objetivo

Produce an inventory and ownership map of the documentation, verify its internal consistency with executable checks, fix what the checks find, and publish the audit report with a prioritized roadmap. The audit is measured, not impressionistic: every number in the report comes from a command recorded next to it.

## What Changes

- `docs/300-development/DOCUMENTATION-AUDIT-2026-10.md`: inventory and ownership map, findings with evidence, fixes applied, residual risks, prioritized documentation roadmap.
- `scripts/test_docs_links.py` (with a CI wrapper): every relative Markdown link in the active documentation resolves; the only exemption carries a reason and an issue.
- Broken relative links in `FRONTEND-DESIGN-SYSTEM.md` fixed; documentation index links the audit.

## Reglas de negocio

| Rule | Source | New / Changed / Made explicit |
|------|--------|-------------------------------|
| Active documentation has no broken relative links | #921, Constitution §8 | Made explicit |

## Capabilities

### New Capabilities

- (none — `skip_specs: true`)

### Modified Capabilities

- (none)

## Impact Analysis

### Módulos afectados

| Module | Touched | What changes |
|--------|---------|--------------|
| `backend-api` | no | — |
| `frontend` | no | — |
| `notaire-shared` | no | — |
| Docs / scripts / CI | yes | docs, one guard |

### Surface area

- Entities / Endpoints / Flyway / Configuration: none
- Dependencies: none

### Architecture review

Documentation only. No ADR.

## Documentation Impact

| Permanent document | What must change |
|--------------------|------------------|
| `docs/300-development/DOCUMENTATION-AUDIT-2026-10.md` | new audit report and roadmap |
| `docs/200-architecture/203-design/FRONTEND-DESIGN-SYSTEM.md` | broken links fixed |
| `CHANGELOG.md` | one line |

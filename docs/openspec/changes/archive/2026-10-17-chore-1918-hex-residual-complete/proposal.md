# Residual legacy jpa/service after ArchUnit empty

> Governed by [CONSTITUTION.md](../../../../CONSTITUTION.md). This proposal documents
> **only this change**; permanent documentation remains the single source of truth.

| Field | Value |
|-------|-------|
| GitHub Issue | #1918 |
| Use Case | CU76 – Quality Assurance and Testing Infrastructure |
| Branch | `cursor/1693-hex-complete-residual-a603` |
| Gate 1 status | passed |

## Objetivo

Close the first mergeable residual slice after ArchUnit R1–R6 = 0: delete dead `service`
singletons, move `PersistenceConstants` out of `jpa`, and record Owner-accepted residual
(#576) in ADR-030 / SAD so #1693 has no dangling undocumented hex debt.

## What Changes

- Delete unused `SessionAdministrator` and `ValidationAdministrator`
- Move `PersistenceConstants` to `com.licensis.notaire.persistence`
- Update ADR-030 / SAD / FLEET-LESSONS: ArchUnit DoD MET; residual = #576
- CHANGELOG Unreleased note

## Reglas de negocio

| Rule | Source | New / Changed / Made explicit |
|------|--------|-------------------------------|
| Legacy packages gain no new dependents; shrink when safe | ADR-030 rule 8 / #1918 | Made explicit |
| Full jpa controller retirement stays on #576 | #576 / #1918 | Made explicit (ADR exception) |

## Capabilities

### New Capabilities

- `hex-residual-legacy`: residual cleanup after empty ArchUnit baseline

### Modified Capabilities

## Impact Analysis

### Módulos afectados

| Module | Touched | What changes |
|--------|---------|--------------|
| `backend-api` | yes | delete 2 dead classes; move PersistenceConstants |
| `frontend` | no | — |
| docs | yes | ADR-030, SAD, FLEET-LESSONS, CHANGELOG |

### Surface area

- Entities: version field still uses INITIAL_VERSION (same value)
- Endpoints / Flyway / config: none

### Architecture review

ADR-030 outcome + residual exception (packages remain until #576).

## Documentation Impact

| Permanent document | What must change |
|--------------------|------------------|
| ADR-030 | status outcome; residual; slice log |
| SAD §5.2 Current state | tip + empty baseline + residual counts |
| FLEET-LESSONS-HEX-1693 | completion note |
| CHANGELOG.md | Unreleased |

## Out of Scope

Deleting all `*JpaController`s (#576); FE issue closes (human paste).

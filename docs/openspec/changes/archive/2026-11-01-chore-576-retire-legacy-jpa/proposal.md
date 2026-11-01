# Retire legacy jpa package and JpaAdministrator

> Governed by [CONSTITUTION.md](../../../../CONSTITUTION.md). This proposal documents
> **only this change**; permanent documentation remains the single source of truth.

| Field | Value |
|-------|-------|
| GitHub Issue | #576 |
| Use Case | CU76 – Quality Assurance and Testing Infrastructure |
| Branch | `chore/576_retire-legacy-jpa` |
| Gate 1 status | passed |

## Objetivo

The Swing-era `com.licensis.notaire.jpa` package (~10k LOC) and `service.JpaAdministrator` /
`LegacyJpaEmfConfig` are a dead island: no adapter or use case calls them. The only live
wire is `IdentificationTypeLookup` catalog bootstrap via `IdentificationTypeJpaController`.
Remove the island so ADR-030 / #1693 DoD ("jpa and service gone") becomes true, and keep
catalog resolution on Spring Data.

## What Changes

- Wire `IdentificationTypeLookup` from `IdentificationTypeRepository` (Spring Data) at
  application start
- Delete `com.licensis.notaire.jpa` (all `*JpaController`s, exceptions, helpers)
- Delete `service.JpaAdministrator` and `service.LegacyJpaEmfConfig`
- Update tests that asserted presence of legacy types; remove JaCoCo excludes for deleted
  packages
- Update ADR-030, SAD current-state notes, FLEET-LESSONS, CHANGELOG

## Reglas de negocio

| Rule | Source | New / Changed / Made explicit |
|------|--------|-------------------------------|
| Persistence for REST goes through hexagonal adapters / Spring Data repositories, never legacy `*JpaController` | ADR-030, #576 | Made explicit (enforced by package deletion) |
| Identification-type name/id resolution for remaining `business` entities uses the shared catalog wired from the live repository | CU76 / IdentificationTypeLookup | Changed (source of catalog) |

## Capabilities

### New Capabilities

- `legacy-jpa-retirement`: absence of `jpa` / `JpaAdministrator` and Spring-Data wiring of IdentificationTypeLookup

### Modified Capabilities

(none — REST contract unchanged)

## Impact Analysis

### Módulos afectados

| Module | Touched | What changes |
|--------|---------|--------------|
| `backend-api` | yes | delete legacy packages; new lookup config; tests; JaCoCo excludes |
| `frontend` | no | — |
| `docs` | yes | ADR-030, SAD, FLEET-LESSONS, CHANGELOG, OpenSpec |
| `infra` / `testing` | no | behaviour unchanged |

### Surface area

- Entities / endpoints / Flyway / OpenAPI: unchanged
- Runtime: EMF no longer held in a static singleton; catalog still loads at startup

### Architecture review

Completes Owner-accepted residual documented in ADR-030 after #1918. ArchUnit R1–R6 stay empty.

## Documentation Impact

| Permanent document | What must change |
|--------------------|------------------|
| `docs/200-architecture/202-ADR/ADR-030-hexagonal-architecture-full-adoption.md` | Residual closed; slice log for #576 |
| SAD current-state / hex notes (cited from ADR-030) | jpa/service residual counts → 0 |
| `docs/300-development/304-ai-sdlc-cloud/FLEET-LESSONS-HEX-1693.md` | Residual #576 done |
| `CHANGELOG.md` | Unreleased note |

## Out of Scope

- Rewriting remaining `business.*` JPA entities into pure domain (#580 family)
- Removing ad-hoc controller try/catch (#579)
- MFA (#1640) or other v0.3 security items

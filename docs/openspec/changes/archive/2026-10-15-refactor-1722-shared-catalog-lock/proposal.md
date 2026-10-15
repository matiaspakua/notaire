# Extract shared catalog lock / in-use rule (follow-up after slice 5)

> Governed by [CONSTITUTION.md](../../../../CONSTITUTION.md). This proposal documents
> **only this change**; permanent documentation remains the single source of truth.

| Field | Value |
|-------|-------|
| GitHub Issue | #1722 |
| Use Case | Architectural refactor under epic #1693 (ADR-030); catalogs CU27 / CU30 / CU35 / CU38 / CU65 / CU67 |
| Branch | `refactor/1722_shared_catalog_lock` |
| Gate 1 status | passed |

## Objetivo

Four catalogs now share the locked-when-in-use shape (identification #1700, folio #1703, document #1705, management status #1719). ADR-030 deferred extracting the shared rule until the fourth was green. Duplicating the update/delete lock and version check in each use case risks silent drift; a small shared helper keeps each catalog's messages and usage ports while one rule owns the control flow.

## Why

Same as Objetivo: extract the shared write-lock control flow after the fourth catalog so the four copies cannot drift, without changing REST bodies.

## What Changes

- A shared application-layer helper (or package-private collaborator) that runs the common **update** path: load → refuse if in use → refuse if stale version → apply change → save; and the common **delete** path: require exists → refuse if in use → delete.
- Folio type, document type and management status management services call the helper; they keep catalog-specific messages, usage ports and domain types.
- Identification types stay on their own path where it already differs (unique name on create/update; lock only on delete) — either adopt the delete half of the helper or document the deliberate exception.
- Existing characterization / referential integrity tests for the four catalogs stay green with **unchanged** HTTP bodies.
- ArchUnit baseline must not grow; shrink if any violation disappears.
- **CHANGELOG omitted** while #1721 holds the Unreleased lane (pure internal refactor; no REST behaviour change).

## Reglas de negocio

| Rule | Source | New / Changed / Made explicit |
|------|--------|-------------------------------|
| A catalog row referenced by dependents cannot be edited or deleted; clients get the same 409 bodies as today | existing controllers / use cases | Made explicit in one shared rule |
| Update refuses a client version that is not the stored one (folio, document, management status) | #1703 / #1705 / #1719 | Unchanged; shared helper hosts the check |
| Identification unique-name and delete-only lock stay as today unless this PR adopts delete via the helper without changing bodies | #1700 / #1361 | Kept or adopted with no HTTP change |

## Capabilities

### New Capabilities

- `shared-catalog-lock`: shared lock / in-use / stale-version control flow for catalog management use cases.

### Modified Capabilities

- None at the REST requirement level (behaviour unchanged). Existing `management-status-hexagon` and sibling catalog specs stay valid.

## Impact Analysis

### Módulos afectados

| Module | Touched | What changes |
|--------|---------|--------------|
| `backend-api` | yes | shared helper + three (or four) management services; unit tests |
| `frontend` | no | — |
| CI/CD | no | — |

### Surface area

- Entities / Flyway / OpenAPI: none
- Endpoints: none (status codes and bodies unchanged)
- CHANGELOG: **omit** while #1721 is open

### Architecture review

Follows ADR-030 (extract after fourth catalog). No new ADR; update ADR-030 slice log.

## Documentation Impact

| Permanent document | What must change |
|--------------------|------------------|
| `docs/200-architecture/202-ADR/ADR-030-hexagonal-architecture-full-adoption.md` | note that the shared lock extract landed (#1722) |
| `CHANGELOG.md` | **omit** while #1721 holds the lane |

## Out of Scope

- Changing any catalog REST contract or error text.
- Frontend hexagon.
- Further catalog migrations (submitted document, folio instance, workflow*, users/roles).
- Extracting query/search/in-use **read** paths into a shared type (only write lock + version).

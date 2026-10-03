# English `@NamedQuery` name strings after domain-schema rename (#973)

> Governed by [CONSTITUTION.md](../../../CONSTITUTION.md). This proposal documents
> **only this change**; permanent documentation remains the single source of truth.

| Field | Value |
|-------|-------|
| GitHub Issue | #1022 |
| Use Case | none — purely technical/internal-quality change (cosmetic JPA NamedQuery identifiers); same exception as epic #973; no user-facing behavior |
| Branch | `cursor/chore-1022-namedquery-english-69d3` |
| Gate 1 status | passed (implementing) |

## Objetivo

Epic #973 Englishized JPA `@Table`/`@Column` mappings and PostgreSQL schema, but
explicitly left `@NamedQuery` *name* strings in Spanish (cosmetic identifiers,
zero migration risk). Entities under `business/*.java` still declare Spanish
prefixes (e.g. `Escritura.findByFechaEscrituracion`) while JPQL bodies already
use English entity/field names. Call sites in `jpa/*JpaController` and unit tests
still pass those Spanish names to `createNamedQuery`. A latent mismatch remains:
`Person` declares `Person.*` but `PersonJpaController` (and mocks) still call
`Persona.*`. This change closes that cosmetic gap in one PR batch rename.

## What Changes

- Rename every Spanish `@NamedQuery(name = "…")` string under
  `backend-api/.../business/*.java` to English entity prefix + English method
  tail (e.g. `Escritura.findByFechaEscrituracion` → `Deed.findByDeedDate`).
- Update every `createNamedQuery("…")` call site in main + test to the new name.
- Fix `Persona.*` call sites to `Person.*` with English method tails.
- Englishize Spanish method tails on already-English prefixes (`Folio`, `Item`,
  `Person`).
- Add a static inventory/hygiene unit test that fails if Spanish NamedQuery
  prefixes (or `Persona.*` createNamedQuery) remain.
- **Not BREAKING** for REST/API/DTO/frontend. No Flyway / schema change.
- **Do not** change JPQL `query = "…"` bodies.

## Reglas de negocio

| Rule | Source | New / Changed / Made explicit |
|------|--------|-------------------------------|
| `@NamedQuery` name strings MUST use English entity prefixes matching the Java entity class (or the #1022 rename map) | #1022 AC; #973 exception | New (naming convention) |
| `createNamedQuery` call sites MUST match declared NamedQuery names | #1022 AC; latent Persona bug | Changed (fix mismatch) |
| JPQL query bodies MUST remain unchanged (already English) | #1022 technical notes | Unchanged |
| No Flyway / schema / API / DTO / frontend renames in this change | #1022 scope | Constraint |

## Capabilities

### New Capabilities

- `jpa-namedquery-english-names`: JPA `@NamedQuery` name strings and
  `createNamedQuery` call sites under `backend-api` use English entity prefixes
  and English method tails; Spanish prefixes and `Persona.*` call sites are
  forbidden by a static hygiene test.

### Modified Capabilities

- (none — naming-only; no observable API/behavior capability under `openspec/specs/`)

## Impact Analysis

### Módulos afectados

| Module | Touched | What changes |
|--------|---------|--------------|
| `backend-api` | yes | `business/*` NamedQuery names; `jpa/*` + unit-test `createNamedQuery` strings; new hygiene test |
| `frontend` | no | — |
| `frontend-swing` | no | Removed / out of scope |
| `notaire-shared` | no | — |
| `infra` / observability | no | — |
| CI/CD (`.github/workflows`) | no | Existing backend CI covers new unit test |
| Scripts | no | — |

### Surface area

- Entities: NamedQuery *name* strings only (no field/table/column changes)
- Endpoints: none
- Database (Flyway `V{n}`): none
- Configuration / `.env`: none
- Dependencies: none
- BREAKING for API clients: no

### Cross-cutting concerns

- Auth/authz: unchanged
- Audit: unchanged
- i18n: n/a (identifiers only)
- Observability: unchanged

## Documentation Impact

| Document | Action |
|----------|--------|
| `CHANGELOG.md` | Add `[Unreleased]` chore note |
| `openspec/changes/archive/.../translate-domain-schema-to-english/` | Historical deferral of #1022 — no edit required |
| Business Use Cases | none (exception: no UC) |

## Out of Scope

- JPQL query body edits
- Flyway / PostgreSQL renames
- REST path / DTO / frontend identifier renames
- Renaming Java entity class names (already English)
- Broad Spanish comment cleanup beyond NamedQuery identifiers in touched call sites
  (English-only rule still applies to strings we edit)

## Success Criteria

- Hygiene test green: zero Spanish NamedQuery prefixes; zero `Persona.*` createNamedQuery
- `mvn test -pl backend-api` green
- Residual Spanish NamedQuery prefix inventory count = 0

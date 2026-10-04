# Unit tests + null-safety for DeedManagement entity (#853)

> Governed by [CONSTITUTION.md](../../../CONSTITUTION.md). This proposal documents
> **only this change**; permanent documentation remains the single source of truth.

| Field | Value |
|-------|-------|
| GitHub Issue | #853 |
| Use Case | CU76 – Quality Assurance and Testing Infrastructure (covers entity null-safety for CU02/CU14/CU19 gestión surfaces) |
| Branch | `cursor/test-853-gestion-escritura-unit-69d3` (create at implement time) |
| Gate 1 status | draft ready (internal stockpile); implement after queue through #799/#800/#805 |

## Objetivo

`DeedManagementEntityTest` on tip has only ~15 tests and does not cover the
null-relationship paths that still NPE in production helpers:
`getDto()` → `fkIdManagementStatus.getDto()`, `getDtoNotary()` when
`fkIdNotaryPerson` or its identification type is null, and
`Person.getDto()` when `DeedManagementList` / identification type is null.
Issue #853 asks for comprehensive unit coverage (target the historical 28-case
suite / 90%+ entity coverage) and null-safe accessors so entity DTO mapping
never throws `NullPointerException` for unset associations.

## What Changes

- Null-safe guards in `DeedManagement.getDto()`, `getDtoNotary()`, and
  `setAtributos()` when status / notary / identification type / procedure list
  iterators are absent. Constructors (default, id, and full) initialize
  `procedureList` / `historyList` to empty `ArrayList`.
- Null-safe guards in `Person.getDto()` for null
  `fkIdIdentificationType` and null `DeedManagementList`.
- Expand `DeedManagementEntityTest` (and targeted `PersonEntityTest` cases) so
  every null-path and happy-path DTO mapping scenario is asserted; all tests
  green; no compilation errors.
- Do **not** change REST contracts beyond safer DTO payloads (null fields
  instead of 500).

## Reglas de negocio

| Rule | Source | New / Changed / Made explicit |
|------|--------|-------------------------------|
| Entity DTO helpers MUST NOT throw NPE when optional relationships are unset | CU76; #853 AC | Made explicit |
| Unset notary / status map to null (or omitted) on DTO, not crash | #853 | Changed |
| List fields initialized by constructor stay empty lists (not null) unless explicitly set null by caller | current entity ctor | Made explicit in tests |
| Coverage for DeedManagement entity methods rises toward 90%+ | CU76; #853 | Made explicit |

## Capabilities

### New Capabilities

- `deed-management-entity-null-safety`: Null-safe DeedManagement/Person DTO
  mapping with unit-test coverage for unset relationships.

### Modified Capabilities

- (none)

## Impact Analysis

### Módulos afectados

| Module | Touched | What changes |
|--------|---------|--------------|
| `backend-api` | yes | Entity null-guards + unit tests |
| `frontend` | no | — |
| `frontend-swing` | no | removed |
| `notaire-shared` | no | DTO shapes unchanged |
| `infra` / observability | no | — |
| CI/CD (`.github/workflows`) | no | — |

### Surface area

- Entities: `DeedManagement`, `Person` (null-safe getters / setAtributos)
- Endpoints: none new; existing gestión/person reads become safer
- Database (Flyway `V{n}`): **no**
- Configuration / `.env`: none
- Dependencies: none
- BREAKING: no

### Architecture review

Entity-layer hardening only; keep mapping helpers on entities for this change
(do not expand scope into a full mapper rewrite). No ADR.

## Documentation Impact

| Permanent document | What must change |
|--------------------|------------------|
| `CHANGELOG.md` | Note null-safety + test coverage under `[Unreleased]` (engineering) |
| CU docs | n/a — behavior is defensive; CU02/CU14 flows unchanged |

## Out of Scope

- Rewriting all entity↔DTO mapping to MapStruct / dedicated mappers.
- Fixing every NPE across the entire `business` package (only DeedManagement +
  Person paths cited by #853).
- Frontend or Playwright work (no UI surface).

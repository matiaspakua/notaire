# Remove the BusinessController god class

> Governed by [CONSTITUTION.md](../../../CONSTITUTION.md). This proposal documents
> **only this change**; permanent documentation remains the single source of truth.

| Field | Value |
|-------|-------|
| GitHub Issue | #900 |
| Use Case | CU76 – Quality Assurance and Testing Infrastructure |
| Branch | `refactor/900_remove_business_controller` |
| Gate 1 status | draft |

## Objetivo

BusinessController (5,337 lines, excluded from coverage) is a singleton façade nobody calls: the REST layer uses services and repositories. Only two of its 127 public methods are reachable from production code, both identification-type lookups called by three entities and one data-access class. Replace those two lookups with a small, tested class and delete the god class.

## What Changes

- New `business/IdentificationTypeLookup` with `nameOf(Integer id)` and `idOf(String name)`; unit-tested with an injected catalog.
- Person, DeedManagement, Procedure and PersonJpaController call the lookup instead of `BusinessController`.
- `BusinessController.java` deleted (0 lines); the three `BusinessController*` coverage excludes in `backend-api/pom.xml` removed.
- `scripts/test_no_business_controller.py` (with a CI wrapper) fails if the class, its imports or its excludes come back.
- SAD debt tables and the CHANGELOG record the removal.

## Reglas de negocio

| Rule | Source | New / Changed / Made explicit |
|------|--------|-------------------------------|
| Production code does not use a singleton business façade; lookups are small injectable classes | #900, ADR-021, Constitution P3 | Made explicit |
| The identification-type lookups match exactly (the old code used `contains`, so id 1 could match id 11 and a name could match a longer one) | #900 | Changed |

## Capabilities

### New Capabilities

- `identification-type-lookup`: Resolve identification-type names and ids without a god class.

### Modified Capabilities

- (none)

## Impact Analysis

### Módulos afectados

| Module | Touched | What changes |
|--------|---------|--------------|
| `backend-api` | yes | new lookup class; four callers; god class deleted |
| `frontend` | no | — |
| `notaire-shared` | no | — |
| Docs / scripts / CI | yes | pom excludes, SAD, CHANGELOG, one static guard |

### Surface area

- Entities / Endpoints / Flyway / Configuration: none
- Dependencies: none

### Architecture review

Removes a legacy singleton; adds no layer. No ADR (consistent with ADR-021 and the repository-over-jpa rule).

## Documentation Impact

| Permanent document | What must change |
|--------------------|------------------|
| `docs/200-architecture/201-SAD/sad.md` | debt tables mark the god class removed |
| `CHANGELOG.md` | one entry |

# refactor: move person transport mapping off the entity

> Governed by [CONSTITUTION.md](../../../../CONSTITUTION.md).

| Field | Value |
|-------|-------|
| GitHub Issue | #2329 |
| Use Case | CU76 — Quality Assurance and Testing Infrastructure |
| Branch | `cursor/2329-person-mapper-47dc` |
| Gate 1 status | passed |

## Objetivo

`Person.setAttributes` copied a `DtoPerson`, including client fields only when the person is a client. `User.setAttributes` was the only caller. This slice moves that copy off the person entity.

## What Changes

- Add `PersonLegacyMapper.apply`.
- `User` copies the same fields itself, because `business` does not import the adapter.
- Delete `Person.setAttributes`.
- Drop that ratchet line.

## Reglas de negocio

| Rule | Source | New / Changed / Made explicit |
|------|--------|-------------------------------|
| Identity fields and the identification type are always copied | existing method | Made explicit |
| Nationality, birth date, tax id, marital status, marriage count, sex, occupation, and address are copied only when the person is a client | existing method | Made explicit |

## Capabilities

### New Capabilities

- None. The HTTP contract does not change.

### Modified Capabilities

- None. `skip_specs: true`.

## Impact Analysis

### Módulos afectados

| Module | Touched | What changes |
|--------|---------|--------------|
| `backend-api` | yes | Mapper, Person, User |
| `frontend` | no | |
| `frontend-swing` | no | |
| `infra` | no | |
| CI/CD | no | |

### Surface area

- Endpoints: none
- Not **BREAKING** for API clients.

### Write set

- `PersonLegacyMapper.java`
- `Person.java`
- `User.java`
- `PersonLegacyMapperTest.java`
- `entity-dto-mapping.txt`

### Architecture review

Follows ADR-030. `User` keeps its own copy of the person fields. Located with `rg` because the symbol was known. This is not a sampled locate pair.

## Documentation Impact

| Permanent document | What must change |
|--------------------|------------------|
| Product docs | none |

## Out of Scope

- `User.setAttributes`, `DeedManagement#getDto`, and `Procedure#getDto`. #2329 stays open.

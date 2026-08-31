# Fix Inmueble.valuacionFiscal type mismatch blocking all Inmueble creation

> Governed by [CONSTITUTION.md](../../../CONSTITUTION.md). This proposal documents
> **only this change**; permanent documentation remains the single source of truth.

| Field | Value |
|-------|-------|
| GitHub Issue | #879 |
| Use Case | CU69 – Gestión de Inmuebles |
| Branch | `fix/879_inmueble-valuacion-fiscal-type` |
| Gate 1 status | pending |

## Objetivo

`Inmueble.valuacionFiscal` (`negocio/Inmueble.java`) is declared as `String`, but the
Flyway-owned column `inmuebles.valuacion_fiscal` is `real` (`V1__initial_schema.sql`).
Hibernate always binds this field with an explicit VARCHAR JDBC type on every INSERT —
so every attempt to create an Inmueble through `POST /api/v1/inmueble` (the endpoint the
`/dashboard/inmuebles` UI calls) fails with `ERROR: column "valuacion_fiscal" is of
type real but expression is of type character varying`, regardless of the value
entered, including an empty one. This was discovered while building a Playwright demo
script that drives the real UI to create example gestiones — Inmueble creation is
unconditionally broken today, not a script or input-formatting issue. This change
aligns the entity/DTO/frontend type with the column that has been `real` since the
schema's first migration.

## What Changes

- `Inmueble.valuacionFiscal` (`negocio/Inmueble.java`) changes from `String` to `Float`
  — the existing convention for equivalent numeric fields
  (`Presupuesto.montoInmueble` is already `Float`).
- `DtoInmueble.valuacionFiscal` (`notaire-shared/.../dto/DtoInmueble.java`), used by
  `Inmueble.getDto()`/`setAtributos()` and the legacy `ControllerNegocio` alta-inmueble
  path, changes from `String` (default `""`) to `Float` (default `null`) to match.
- Frontend: `Inmueble.valuacionFiscal` (`frontend/src/types/index.ts`) changes from
  `string` to `number`; `frontend/src/app/dashboard/inmuebles/page.tsx`'s create/edit
  form converts its text-input string to a number (or `undefined` when empty) before
  posting, instead of forwarding the raw string.
- **BREAKING**: any API consumer sending/reading `valuacionFiscal` as a JSON string on
  `POST/PUT/GET /api/v1/inmueble*` now sends/receives a JSON number. No known consumer
  besides the Next.js frontend exists (verified via grep of `frontend/src` and the
  Swing DTO usage, both updated in this change); `frontend-swing` is deprecated and not
  built (see `CLAUDE.md`), so it is not a live consumer either.

## Reglas de negocio

| Rule | Source | New / Changed / Made explicit |
|------|--------|-------------------------------|
| An Inmueble's valuación fiscal, when provided, is a numeric monetary amount, not free text | CU69 – Gestión de Inmuebles (the field represents an assessed property value) | Made explicit — the schema already required this (`real` column since `V1`); the entity/DTO/frontend types simply did not match it |

## Capabilities

### New Capabilities
- `inmueble-valuacion-fiscal`: the Inmueble's valuación fiscal field — its type,
  optionality, and that creating/updating an Inmueble with or without it succeeds
  against the real schema.

### Modified Capabilities
_None — no existing capability spec under `openspec/specs/` covers Inmueble fields yet._

## Impact Analysis

### Módulos afectados

| Module | Touched | What changes |
|--------|---------|---------------|
| `backend-api` | yes | `Inmueble.valuacionFiscal` field/getter/setter type; `Inmueble.getDto()`/`setAtributos()` mapping |
| `notaire-shared` | yes | `DtoInmueble.valuacionFiscal` field/getter/setter/constructor type |
| `frontend` | yes | `Inmueble.valuacionFiscal` type; `inmuebles/page.tsx` form state and payload construction |
| `frontend-swing` | no | Deprecated, not built (`CLAUDE.md`); not touched |
| `infra` / observability | no | — |
| CI/CD (`.github/workflows`) | no | — |

### Surface area

- Entities: `Inmueble` (`negocio/Inmueble.java`) — `valuacionFiscal` type only, no new
  fields, no relation changes.
- Endpoints: `POST/PUT/GET /api/v1/inmueble*` (`InmuebleController`) — request/response
  JSON shape for `valuacionFiscal` changes from string to number. No new endpoints.
- Database: none — no migration needed, the column has always been `real`.
- Configuration / `.env`: none.
- Dependencies: none.

### Architecture review

Purely a type-alignment fix within existing architecture; no new pattern, no ADR
required. Follows CLAUDE.md's numeric-field convention already used by
`Presupuesto.montoInmueble` (`Float`).

## Documentation Impact

| Permanent document | What must change |
|---------------------|-------------------|
| `docs/100-business/102-use-cases/CU69 – Gestión de Inmuebles.md` | Note the fix if the document describes the field's data type |
| `CHANGELOG.md` | Add an `[Unreleased]` entry: Inmueble creation was broken (type mismatch), now fixed; `valuacionFiscal` API shape changes from string to number (**BREAKING**) |

## Out of Scope

- Any new validation UI (min/max, currency formatting) for the field beyond making the
  existing text input send a valid number.
- Migrating `frontend-swing`'s `DtoInmueble` usage — the module is deprecated and not
  built.

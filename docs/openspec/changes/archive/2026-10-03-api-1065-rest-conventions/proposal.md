# REST convention ADR, 201+Location, remove unused /pagos/params

> Governed by [CONSTITUTION.md](../../../CONSTITUTION.md). This proposal documents
> **only this change**; permanent documentation remains the single source of truth.

| Field | Value |
|-------|-------|
| GitHub Issue | #1065 |
| Use Case | CU76 — Quality Assurance and Testing Infrastructure |
| Branch | `cursor/api-1065-rest-conventions-69d3` |
| Gate 1 status | passed |

## Objetivo

The REST surface mixes language, singular/plural nouns, search verbs, and create
status codes; ADR-003 covers versioning only. Slice-1 records naming conventions,
removes the unused `POST /pagos/params` endpoint, and standardizes successful
creates on `201 Created` with a `Location` header via a shared helper.

## What Changes

- **ADR-023** documents REST resource naming (language, plural nouns, search path,
  action sub-resources) and a phased rename policy under ADR-003 versioning.
- **BREAKING:** remove unused `POST /api/v1/pagos/params` (unit-tested only; no UI
  or Bruno callers).
- **BREAKING (status):** `POST /api/v1/minutas-inscripcion` returns `201 Created`
  instead of `200 OK` on successful generate/create.
- Add a shared `Location` response helper and emit `Location` on sample creates
  (`POST /api/v1/pagos`, `POST /api/v1/folio`, `POST /api/v1/minutas-inscripcion`).
- Update REST endpoint registry and `CHANGELOG.md` with the BREAKING notes.
- Phased resource renames (singular→plural, language alignment) are decided in
  the ADR but **not** executed as a big-bang in this slice.

## Reglas de negocio

| Rule | Source | New / Changed / Made explicit |
|------|--------|-------------------------------|
| Successful resource creates SHALL return HTTP 201 with a `Location` URI of the created resource | CU76 / REST conventions | Made explicit |
| Unused alternate create surfaces with no UI/Bruno callers SHALL be removed | CU76 / #1065 | New |
| Public REST path naming follows ADR-023 (English resource nouns matching existing `/api/v1` paths where already established; plural collection nouns; `/search`; actions as sub-resources) | CU76 / ADR-023 | New |
| Breaking API changes follow ADR-003 (version bump / deprecation policy for renames) | ADR-003 | Made explicit (this slice removes dead endpoint + status fix only) |

## Capabilities

### New Capabilities

- `rest-api-conventions`: Naming ADR decisions, create `201`+`Location` contract,
  and removal of unused payment params create route.

### Modified Capabilities

- `pagos`: Remove the query-param create alternate; JSON create remains and gains
  `Location`.
- `minuta-inscripcion`: Successful generate returns `201` + `Location`.

## Impact Analysis

### Módulos afectados

| Module | Touched | What changes |
|--------|---------|--------------|
| `backend-api` | yes | PaymentController, RegistrationDraftController, FolioController, Location helper, unit tests |
| `frontend` | no | Does not call `/pagos/params`; create status change is backend contract only |
| `frontend-swing` | no | Removed from repo |
| `notaire-shared` | no | — |
| `infra` / observability | no | — |
| CI/CD (`.github/workflows`) | no | — |

### Surface area

- Entities: none (no schema change)
- Endpoints: remove `POST /api/v1/pagos/params`; status/`Location` on
  `POST /api/v1/pagos`, `POST /api/v1/folio`, `POST /api/v1/minutas-inscripcion`
- Database (Flyway `V{n}`): none
- Configuration / `.env`: none
- Dependencies: none
- **BREAKING:** `/pagos/params` removed; minuta create status `200`→`201`

### Architecture review

Follows adapter/in/web hexagonal controllers. New architectural decision:
**ADR-023 REST resource naming conventions** (ADR-003 remains versioning-only).

## Documentation Impact

| Permanent document | What must change |
|--------------------|------------------|
| `docs/200-architecture/202-ADR/ADR-023-rest-resource-naming.md` | New ADR (Accepted) |
| `docs/200-architecture/202-ADR/README.md` | Index ADR-023 |
| `docs/200-architecture/203-design/REST-API-ENDPOINT_REGISTRY.md` | Drop `/pagos/params`; note 201+Location on sample creates |
| `CHANGELOG.md` | `[Unreleased]` BREAKING entries |

## Out of Scope

- Big-bang renames of singular/Spanish paths (`/folio`→`/folios`, `/people` language
  alignment, `/buscar`→`/search` everywhere) — tracked as follow-up work under
  ADR-023 phased migration, not this PR.
- Applying `Location` to every create endpoint in the API (sample set only in Slice-1).
- Introducing `/api/v2` for renames (future when a rename batch ships).

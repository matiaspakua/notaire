Governed by [CONSTITUTION.md](../../../CONSTITUTION.md) — §6 Quality Gates,
§7 Testing Rules, §11 Release Rules.

## Context

`InmuebleController` (`api/InmuebleController.java`) binds `POST`/`PUT`
request bodies directly to the `Inmueble` entity (`negocio/Inmueble.java`) —
there is no request DTO on this path. Hibernate infers the JDBC bind type for
`valuacionFiscal` from the Java field type; today that's `String`, so every
INSERT/UPDATE explicitly binds a VARCHAR against the `real` column and
Postgres rejects it with a type-mismatch error. H2 (used by
`ApiH2IntegrationTest`-style tests) does not enforce this strictness, which is
why the existing test suite is green despite the bug — only a Postgres-backed
test exposes it (same pattern already established for
`GestionReingresoDocumentacionPgIntegrationTest`, CU43).

A second, legacy path exists: `ControllerNegocio.darAltaInmueble(DtoInmueble)`
(part of the deprecated `frontend-swing` client chain, not called by the
Next.js frontend or the live REST API) uses `DtoInmueble`
(`notaire-shared/.../dto/DtoInmueble.java`), also `String`-typed today.

## Goals / Non-Goals

**Goals:**
- Make `Inmueble.valuacionFiscal` a numeric type that matches the `real`
  column, so REST creation/update works.
- Keep `DtoInmueble.valuacionFiscal` numeric too, so `Inmueble.getDto()` /
  `setAtributos()` stay type-consistent (no silent string↔number coercion at
  the boundary).
- Update the Next.js form so it sends/reads a number.

**Non-Goals:**
- No new validation rules (min/max, currency formatting) beyond what "is a
  valid number" requires.
- No change to `frontend-swing` (deprecated, not built).
- No schema/migration change — the column has been `real` since `V1`.

## Decisions

- **Entity type: `Float`, not `BigDecimal` or `Double`.** Matches the
  existing sibling numeric money field `Presupuesto.montoInmueble` (`Float`),
  keeping the codebase's established convention for this kind of field
  instead of introducing a second one.
- **`DtoInmueble.valuacionFiscal` also becomes `Float`**, kept in scope (not
  deferred) because `Inmueble.getDto()`/`setAtributos()` directly assign
  between the two fields — leaving the DTO as `String` would just move the
  type mismatch one hop over and reintroduce silent String↔Float coercion
  bugs at that boundary.
- **No new DTO layer introduced for `InmuebleController`.** Out of scope: the
  controller binding the entity directly to the request body is a pre-existing
  pattern shared with other `*Controller` classes in this codebase; changing
  that architecture is a separate, larger concern than this bug fix.

## Riesgos / Trade-offs

- Existing Inmueble rows with non-numeric or malformed string values in the
  `valuacion_fiscal` column would fail to load once the entity field is
  `Float` → Mitigation: the column has always been `real` at the database
  level, so no row can contain a non-numeric value; Postgres itself already
  guarantees this invariant.
- Frontend/API consumers currently sending `valuacionFiscal` as a JSON string
  will break (see proposal.md's BREAKING note) → Mitigation: only known
  consumer is this repo's own Next.js frontend, updated in the same change.

## Testing Strategy

| Scenario (spec) | Test level | Test class / file |
|-----------------|------------|-------------------|
| Create Inmueble with a numeric valuación fiscal | integration (Postgres) | `InmuebleValuacionFiscalPgIntegrationTest#shouldCreateInmuebleWithNumericValuacionFiscal` |
| Create Inmueble without a valuación fiscal | integration (Postgres) | `InmuebleValuacionFiscalPgIntegrationTest#shouldCreateInmuebleWithNullValuacionFiscal` |

While writing this test class, `PUT /api/v1/inmueble/{id}` was found to throw
an unrelated `NullPointerException` on `tramiteList` for *any* Inmueble
update, independent of `valuacionFiscal`'s type — filed as
[Issue #880](https://github.com/matiaspakua/notaire/issues/880) and
deliberately kept out of this change's scope (see Riesgos / Trade-offs).

- New integration tests (`src/test/java/.../integration/`):
  `InmuebleValuacionFiscalPgIntegrationTest`, `@Tag("pg-integration")`,
  MockMvc against the real `InmuebleController`, mirroring
  `GestionReingresoDocumentacionPgIntegrationTest`'s pattern.
- Existing unit/integration tests updated in place (see Regression Strategy).
- Coverage impact: negligible size delta; ratchet floor (70% line / 25%
  branch) expected to hold — no new branches introduced, only a type change.

## Regression Strategy

- Existing tests affected (all currently pass String literals into
  `valuacionFiscal`, hiding the bug via H2's laxer type enforcement):
  - `EntitiesBasicTest` (`unit/`) — `setValuacionFiscal("10000")` → numeric
    literal.
  - `InmuebleRepositoryIntegrationTest` (H2) — several string
    `setValuacionFiscal`/`getValuacionFiscal` calls → numeric.
  - `InmuebleServiceIntegrationTest` (H2) — string CRUD assertions plus
    `Integer.parseInt(i.getValuacionFiscal())` range checks → direct `Float`
    comparisons, `parseInt` calls removed.
  - `TramiteEntityTest` — check `DtoInmueble` usage, update if it sets
    `valuacionFiscal`.
- Full suite command: `mvn verify -pl backend-api`
- Postgres-specific command: `mvn test -Ppg-integration`
- HTTP/Bruno API suite: `bash testing/scripts/test.sh`
- Legacy paths at risk: `jpa.InmuebleJpaController` (uses the entity directly,
  no change needed beyond the entity's own type); `frontend-swing` not
  touched (deprecated, not built).

## Playwright Strategy

- Specs add under `frontend/tests/e2e/`: `cu69-inmuebles-valuacion-fiscal.spec.ts`
- Golden path covered: create an Inmueble via `/dashboard/inmuebles` with a
  numeric valuación fiscal, verify it is saved and displayed.
- Edge / error paths covered: create an Inmueble leaving valuación fiscal
  empty, verify it still succeeds and displays a placeholder (`—`).
- Viewports: 320px (mobile) / 768px (tablet) / 1024px (desktop)
- Command: `cd frontend && npx playwright test cu69-inmuebles-valuacion-fiscal`

## Deployment Strategy

- Flyway migration required: no — column has been `real` since `V1`.
- Deployment order / coupling: backend and frontend must deploy together
  (API request/response shape for `valuacionFiscal` changes from string to
  number simultaneously on both sides).
- Configuration or `.env` keys to add: none.
- Feature flag: no.
- Smoke test after deploy (Gate 5): create an Inmueble via
  `POST /api/v1/inmueble` with a numeric `valuacionFiscal` against the
  running stack; confirm `201 Created` and a numeric value in the response.

## Rollback Strategy

- Revert safe: yes — reverting the merge commit restores the prior (broken)
  String type; no data was writable under the new behavior that couldn't
  already be written under the old broken one (Inmueble creation was
  completely blocked before this fix).
- Database rollback: none needed — no migration was added.
- Data written under new behavior after revert: any Inmueble created after
  this fix and before a rollback would already hold a numeric
  `valuacion_fiscal` value in Postgres (the column's native type); reverting
  the Java type back to `String` would make those existing rows fail to load
  until re-fixed. This is the only meaningful rollback risk.
- Blast radius if rollback delayed: none — delaying rollback simply keeps
  Inmueble creation working.

## Open Questions

None.

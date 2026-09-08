> Governed by [CONSTITUTION.md](../../../CONSTITUTION.md) — §6 Quality Gates,
> §7 Testing Rules, §11 Release Rules.

## Context

`Folio.fkIdEscritura` (`@ManyToOne`, `@JoinColumn(name = "fk_id_escritura")`)
already exists in the entity and in the schema, but `Folio.setAtributos`/`getDto`
never read or write it, so `DtoFolio.escritura` is dead on both sides.
`FolioController` already models a `Utilizado` status with a 409 conflict check
on `update`/`delete` (`ESTADO_UTILIZADO`) — this change reuses that mechanism
instead of adding a new status. `Testimonio.fkIdEscritura` is already fully
wired end to end and is out of scope. `CopiaController.create` already links
`Copia` to `Testimonio` but performs no business validation before saving. See
proposal.md — Objetivo, What Changes.

## Goals / Non-Goals

**Goals:**
- Wire the existing `Folio ↔ Escritura` relationship through the DTO layer so
  it can be set and read via the API.
- Enforce the "one active escritura per folio" rule at the point where a folio
  is created or edited.
- Enforce the "no copy of an already-inscripto testimonio" rule at the point
  a copy is created.
- Make the escritura↔folio link visible in the two screens that already show
  folios and escrituras.

**Non-Goals:**
- Introducing a new `Ocupado` status (see proposal.md — Out of Scope).
- Building the cuaderno-rooted protocol chain view (deferred to change 8,
  issue #839).
- Supporting multiple folios per escritura in a single UI action (each folio
  is linked individually; the data model already allows several `Folio` rows
  to point at the same `Escritura`).

## Decisions

- **Reuse `ESTADO_UTILIZADO` instead of adding a new status.** `FolioController`
  already treats `estado = "Utilizado"` as "not free to reassign" for
  update/delete. Alternative considered: add a distinct `"Ocupado"` status as
  the Issue's AC literally says — rejected because it would model the same
  concept twice (`Utilizado` vs `Ocupado`) with no behavioral difference,
  increasing branching for no benefit. If the business later needs a real
  three-way distinction, that is a separate, explicit change.
- **Validate in the controller layer, not a new service.** `FolioController`
  and `CopiaController` already contain their business validation directly
  (e.g. the existing `ESTADO_UTILIZADO` conflict check). Alternative
  considered: extract a `FolioService`/`CopiaService` — rejected as
  out-of-scope refactoring; this change follows the existing pattern (P1 KIS,
  refactoring.md's documented Controller-does-validation style for this
  codebase) rather than introducing a new layer for two checks.
- **`escrituraId` is optional on `FolioRequest`.** Alternative considered:
  require it — rejected, folios that don't yet occupy an escritura are a
  normal, existing state (e.g. freshly purchased folio books) and must remain
  representable.
- **No Flyway migration.** `fk_id_escritura` already exists on `folios`
  (`Folio.java` already maps it) — confirmed by reading the entity; this is a
  DTO/controller wiring change only.

## Riesgos / Trade-offs

- [Reusing `Utilizado` conflates "folio has been used for any escritura" with
  "folio is currently occupied by exactly this escritura"] → Mitigation: the
  409 check compares `fkIdEscritura` identity, not just the status flag, so
  re-saving the same folio with the same escritura is accepted (idempotent),
  and only a genuine second escritura trying to claim an already-`Utilizado`
  folio is rejected.
- [Frontend selector for "escrituras without a folio yet" requires a query
  across escrituras and their linked folios that doesn't exist today] →
  Mitigation: implemented as a client-side filter over the existing
  `GET /api/v1/escrituras` list (`estado = "Firmada"` and no `folios` entry)
  rather than a new backend endpoint, since the dataset size in this domain
  (notarial office, not a nationwide database) makes a dedicated filtered
  endpoint premature; revisit if the escrituras list ever needs pagination
  filtering server-side.
- [`Escritura.folioList` has no `CascadeType.ALL`] → Mitigation: not needed
  by this change — the link is always written from the `Folio` (owning) side
  via `fkIdEscritura`, never through the `Escritura` inverse collection, so
  the missing cascade on the inverse side is irrelevant to this change's
  write path. Documented here so a future change touching `Escritura.folioList`
  doesn't rediscover this from scratch.

## Testing Strategy

| Scenario (spec) | Test level | Test class / file |
|-----------------|------------|-------------------|
| Alta de folio vinculado a una escritura | integration | `FolioControllerTest#shouldLinkFolioToEscrituraOnCreate` |
| Edición de folio para vincularlo a una escritura | integration | `FolioControllerTest#shouldLinkFolioToEscrituraOnUpdate` |
| Alta de folio sin vincular | integration | `FolioControllerTest#shouldCreateFolioWithoutEscritura` |
| Folio ya vinculado a otra escritura | integration | `FolioControllerTest#shouldRejectLinkingFolioAlreadyUtilizadoByAnotherEscritura` |
| Re-vincular el mismo folio a la misma escritura | integration | `FolioControllerTest#shouldAllowReSavingFolioWithSameEscritura` |
| Consultar un folio con escritura vinculada | unit | `FolioTest#shouldRoundTripEscrituraThroughDto` |
| Consultar un folio sin escritura vinculada | unit | `FolioTest#shouldReturnNullEscrituraWhenNotLinked` |
| Testimonio con movimiento inscripto | integration | `CopiaControllerTest#shouldRejectCopiaWhenTestimonioHasInscriptaMovimiento` |
| Testimonio sin movimientos inscriptos | integration | `CopiaControllerTest#shouldCreateCopiaWhenTestimonioHasNoInscriptaMovimiento` |
| Testimonio sin movimientos registrados | integration | `CopiaControllerTest#shouldCreateCopiaWhenTestimonioHasNoMovimientos` |

- New unit tests (`src/test/java/.../unit/`): `FolioTest#shouldRoundTripEscrituraThroughDto`, `FolioTest#shouldReturnNullEscrituraWhenNotLinked`.
- New integration tests (`src/test/java/.../integration/`): `FolioControllerTest` (5 new cases above), `CopiaControllerTest` (3 new cases above).
- Coverage impact: additive branches in `Folio.setAtributos`/`getDto`, `FolioController.create`/`update`, `CopiaController.create`, all directly exercised by the tests above — expected to hold or raise the JaCoCo ratchet floor, never lower it.

## Regression Strategy

- Existing tests affected: `FolioControllerTest` (existing create/update/delete
  cases must keep passing unchanged — `escrituraId` is optional and additive),
  `CopiaControllerTest` (existing create case with a testimonio that has no
  inscripto movement must keep passing unchanged).
- Full suite command: `mvn verify -pl backend-api`
- HTTP/Bruno API suite: `bash testing/scripts/test.sh`
- Legacy paths at risk: none — `jpa` package and `frontend-swing` do not touch
  `Folio`/`Copia`.

## Playwright Strategy

- Specs to add/update under `frontend/tests/e2e/`: `folios-vinculacion.spec.ts`.
- Golden path covered: create a folio, link it to a `Firmada` escritura without
  a folio yet, confirm the folio shows `Utilizado` and the escritura shows the
  folio number.
- Edge / error paths covered: attempt to link a folio already `Utilizado` by
  another escritura and confirm the 409/error message is shown; create a folio
  without selecting an escritura and confirm it saves as before.
- Viewports: 320px (mobile) / 768px (tablet) / 1024px (desktop)
- Command: `cd frontend && npx playwright test`

## Deployment Strategy

- Flyway migration required: no — `folios.fk_id_escritura` already exists.
- Deployment order / coupling: backend and frontend ship together (frontend
  form field depends on backend accepting `escrituraId`); no ordering
  constraint beyond the normal single-deploy path.
- Configuration or `.env` keys to add: none.
- Feature flag: no.
- Smoke test after deploy (Gate 5): create a folio via the UI linked to an
  existing signed escritura, confirm it returns `Utilizado` and the escritura
  detail shows the folio; `GET /actuator/health` green.

## Rollback Strategy

- Revert safe: yes — the change is additive (new optional field, new 409 path
  that previously did not exist); reverting removes the field and the
  validation without touching existing data (`fk_id_escritura` values written
  under the new behavior remain valid rows, just unreadable again via the API
  until re-deployed).
- Database rollback: none needed — no migration was applied.
- Data written under the new behavior after revert: `folios.fk_id_escritura`
  values already set stay in the database untouched; they simply stop being
  exposed through the API until the change is re-deployed.
- Blast radius if rollback is delayed: none beyond the folio/escritura linking
  feature being unavailable; no other capability depends on this change.

## Open Questions

None — the two questions raised during exploration (status literal, cuaderno
chain scope) are resolved in proposal.md — Out of Scope, not deferred.

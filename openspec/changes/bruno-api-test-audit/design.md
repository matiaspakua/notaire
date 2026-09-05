> Governed by [CONSTITUTION.md](../../../CONSTITUTION.md) — §6 Quality Gates,
> §7 Testing Rules, §11 Release Rules.

## Context

`backend-api/api-test/` uses the Bruno YAML/OpenCollection format (per
`backend-api/api-test/README.md`), run via the `bru` CLI against the Docker
stack started by `scripts/start.sh`. Several domain folders already have
partial/complete coverage (`personas`, `presupuestos`, `suplencias`, etc.);
`historial`, `items`, `pagos`, `tramites` were left as bare-stub WIP (a
`folder.yml` only). While reconstructing correct request payloads for these
stubs, deserialization of `Item.fkIdPresupuesto` was found to silently no-op
(`@JsonIgnore`), which zeroed out `PagoService.calcularSaldoPendiente()` and
made the `pagos` happy-path un-testable without first fixing the entity.

## Goals / Non-Goals

**Goals:**
- Finish `historial`, `items`, `pagos`, `tramites` with full CRUD lifecycles
  and chai assertions, self-contained (each folder creates its own
  dependencies via numbered setup steps, per existing `suplencias`/`folios`
  convention).
- Fix the `Item.fkIdPresupuesto` visibility bug so the `pagos` happy path is
  actually exercisable.
- Bring `auditoria`, `folios`, `inmueble`, `plantilla-presupuesto`,
  `suplencias`, `00-auth` WIP to a complete, documented state.
- Regenerate `COVERAGE.md` and reconcile `TEST-PLAN.md`/`CU-API-MATRIX.csv`
  with the real, current state (not an aspirational 100%).

**Non-Goals:**
- Building out the 16 controllers with zero existing Bruno coverage
  (`Gestion`, `Testimonio`, workflow controllers, etc.) — tracked as a
  follow-up Issue (see proposal.md Out of Scope).
- Any frontend/UI change — this is API-contract-test and backend-entity work
  only.

## Decisions

- **Fix via `@JsonProperty(access = WRITE_ONLY)` rather than removing
  `@JsonIgnore` outright.** A plain writable field would put the full nested
  `Presupuesto` object (its own item/pago/tramite lists) into every `Item`
  response body, risking infinite recursion / huge payloads on serialization.
  `Tramite.fkIdPresupuesto` (no annotation at all) already exhibits the
  "included on read" behavior for comparison, but `Item` is a leaf entity
  read far more often (e.g. `GET /api/v1/items`), so keeping it write-only
  matches the existing pattern of exposing FK-only writes without full graph
  reads.
- **Self-contained Bruno folders over shared cross-folder fixtures.** Follows
  the existing `suplencias/00a-*`/`00b-*` and `folios/00-*` convention: each
  folder creates the reference rows it needs (Gestion, Presupuesto, Persona)
  rather than depending on a specific run order across folders, since Bruno
  runs folders in alphabetical order and a hard cross-folder dependency is
  fragile.

## Riesgos / Trade-offs

- [Fixing `Item.fkIdPresupuesto` changes real backend behavior, not just
  tests] → Behavior change is a bug fix restoring the balance-calculation
  contract CU15 already implies (a budget's total must include its items);
  verified manually via `curl` + DB inspection before and after, and covered
  going forward by the new `items`/`pagos` Bruno assertions.
- [16 controllers remain with zero Bruno coverage after this change] →
  Documented explicitly in `COVERAGE.md`, `TEST-PLAN.md` and a new follow-up
  GitHub Issue rather than left as a silent gap.

## Testing Strategy

| Acceptance Criterion (Issue #952) | Test level | Test class / file |
|---|---|---|
| Item create/update persists budget FK | API contract | `backend-api/api-test/items/*.yml` |
| Pago happy path succeeds against real balance | API contract | `backend-api/api-test/pagos/*.yml` |
| Historial/Tramite CRUD covered | API contract | `backend-api/api-test/historial/*.yml`, `backend-api/api-test/tramites/*.yml` |
| Full suite green | API contract (regression) | `bru run . -r --env Developmen` |

- New unit tests: none — the fix is a Jackson visibility annotation on an
  existing entity; behavior is proven via the Bruno contract tests above,
  which is the correct level for this kind of I/O-boundary defect (a unit
  test would need to mock Jackson/Hibernate and would not have caught the
  original bug either).
- New integration tests: none beyond the Bruno suite (no schema/repository
  change).
- Coverage impact (JaCoCo ratchet floor; 70% line / 25% branch): unaffected —
  no new Java production code paths, single annotation swap on an existing
  field.

## Regression Strategy

- Existing tests affected: none expected to break — `Item` serialization
  shape for existing consumers (frontend `items` reads) does not change
  ( `fkIdPresupuesto` was never serialized before under `@JsonIgnore`, and
  still isn't under `WRITE_ONLY`).
- Full suite command: `mvn verify -pl backend-api`
- HTTP/Bruno API suite: `bash backend-api/api-test/run.sh` (or `bru run .
  -r --env Developmen` from `backend-api/api-test/`)
- Legacy paths at risk: none — `Item`/`Pago`/`Tramite` are already on the
  `repository`/`service` path, not legacy `jpa`.

## Playwright Strategy

n/a - no UI surface. This change only touches backend entity serialization
and the Bruno API contract-test suite.

## Deployment Strategy

- Flyway migration required: no
- Deployment order / coupling: none — single backend jar rebuild, no
  coordinated frontend/schema change
- Configuration or `.env` keys to add: none
- Feature flag: no
- Smoke test after deploy (Gate 5): `POST /api/v1/items` with a
  `fkIdPresupuesto` reference, then `GET /api/v1/pagos/presupuesto/{id}/saldo`
  reflects the item's value

## Rollback Strategy

- Revert safe: yes — reverting the annotation change restores the prior
  (buggy but stable) behavior; no data migration involved.
- Database rollback: none needed
- Data written under the new behavior after revert: items created with a
  correctly-persisted `fk_id_presupuesto` remain valid rows if reverted; only
  future writes would regress to the old (silently-dropped) behavior.
- Blast radius if rollback is delayed: none — fix is strictly corrective.

## Open Questions

None.

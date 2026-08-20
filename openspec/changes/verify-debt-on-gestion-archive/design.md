> Governed by [CONSTITUTION.md](../../../CONSTITUTION.md) — §6 Quality Gates,
> §7 Testing Rules, §11 Release Rules.

## Context

See `proposal.md` — Objetivo. Two relevant facts about the current codebase
shape this design:

- A saldo-pendiente calculation already exists per presupuesto:
  `PagoService.calcularSaldoPendiente(Integer idPresupuesto)`, exposed at
  `GET /api/v1/pagos/presupuesto/{idPresupuesto}/saldo`
  (`backend-api/.../api/PagoController.java:84-94`). This change aggregates
  that per-presupuesto calculation across a gestión's trámites — it does not
  reimplement it.
- `GestionDeEscritura` (table `gestiones_de_escrituras`) holds a
  `List<Tramite> tramiteList`; each `Tramite` links to one `Presupuesto` via
  `fkIdPresupuesto` (`negocio/Tramite.java:108-110`). This is the path used to
  aggregate saldo per gestión.
- Archiving a gestión today (setting its estado to `"Archivada"`) only exists
  in the legacy path: `ControllerNegocio.archivarGestion(...)` →
  `GestionDeEscrituraJpaController.archivarGestiones(...)`
  (`negocio/ControllerNegocio.java:1620-1656`). The modern
  `api/GestionController.java` (repository-backed) has no archive endpoint,
  and no frontend screen calls the legacy one — archiving is not reachable
  from the current UI at all.

## Goals / Non-Goals

**Goals:**
- Expose a gestión-archiving action in the modern REST layer
  (`api`/`service`/`repository`) that a user can actually invoke from the UI.
- Calculate the aggregate pending balance across all presupuestos of a
  gestión's trámites before that action completes.
- Warn on debt and persist the debt-at-archive flag, per the delta spec.

**Non-Goals:**
- Migrating or removing the legacy `ControllerNegocio.archivarGestion` /
  `GestionDeEscrituraJpaController` path — out of scope; it is left in place
  and unused by the new endpoint, consistent with the project's incremental
  `jpa` → `repository` migration already under way elsewhere.
- Building the full pago↔presupuesto↔gestión financial summary view — that is
  issue #820; this change only needs the aggregate number, not the itemized
  breakdown.
- Installment plans, discount/surcharge modeling, document costs — issues
  #821, #822, #823.

## Decisions

- **Add the archive endpoint to `api/GestionController.java` /
  `service` / `repository`, not to the legacy `jpa` package.**
  Alternative considered: extend `GestionDeEscrituraJpaController` since the
  archiving logic already lives there. Rejected — `CLAUDE.md` and
  `.claude/rules/refactoring.md` are explicit that new code uses `repository`,
  not `jpa`, and the legacy path is unreachable from the UI today anyway, so
  there is no existing consumer to preserve.
- **Aggregate saldo by summing `PagoService.calcularSaldoPendiente` per
  presupuesto linked through `tramiteList`**, rather than writing a new
  gestión-level SQL aggregate query.
  Alternative considered: a single `@Query` joining `gestiones_de_escrituras`
  → `tramites` → `presupuestos` → `pagos` for a database-side sum. Rejected
  for this change — the per-presupuesto calculation already exists and is
  covered elsewhere; a database-side aggregate is deferred to issue #820,
  which needs it for the itemized financial summary anyway and can revisit
  this decision then.
- **Warning is advisory, not blocking**: the archive endpoint accepts the
  archive request regardless of pending debt; the client is responsible for
  showing the warning and asking for confirmation before calling it.
  Alternative considered: a two-step API (`check` then `confirm`) forcing the
  warning server-side. Rejected as unnecessary complexity — RF-22 requires
  *advertir*, not a server-enforced confirmation gate, and the caller already
  has the saldo before submitting.

## Riesgos / Trade-offs

- [Aggregating in application code (Java loop) instead of SQL could be slow
  for a gestión with many trámites] → Mitigation: gestiones in this domain
  have a small, bounded number of trámites (notarial case load); revisit with
  a SQL aggregate only if issue #820 profiling shows it matters.
- [Introducing a new archive endpoint while the legacy one still exists could
  let the two diverge] → Mitigation: the legacy path is not called by any
  current UI; document this explicitly in CU16 so a future agent does not
  wire the frontend to the legacy endpoint by mistake.
- [The debt-at-archive persisted flag needs a schema change] → Mitigation:
  additive Flyway migration only (new column with a default), no risk to
  existing data — see Deployment Strategy.

## Testing Strategy

| Scenario (spec) | Test level | Test class / file |
|-----------------|------------|-------------------|
| Gestión with a single trámite and presupuesto | unit | `GestionArchiveDebtServiceTest` |
| Gestión with multiple trámites and presupuestos | unit | `GestionArchiveDebtServiceTest` |
| Gestión with no pending balance | unit | `GestionArchiveDebtServiceTest` |
| Archiving a gestión with pending debt | integration | `GestionArchiveIntegrationTest` |
| Confirming archiving despite pending debt | integration | `GestionArchiveIntegrationTest` |
| Archiving a gestión with no pending debt | integration | `GestionArchiveIntegrationTest` |
| Archiving record reflects pending debt | integration | `GestionArchiveIntegrationTest` |
| Archiving record reflects no pending debt | integration | `GestionArchiveIntegrationTest` |

- New unit tests (`src/test/java/.../unit/`): `GestionArchiveDebtServiceTest`
  covering the saldo-aggregation logic in isolation (mocked
  `PagoService`/repositories).
- New integration tests (`src/test/java/.../integration/`):
  `GestionArchiveIntegrationTest` covering the archive endpoint end-to-end
  against PostgreSQL (`mvn test -Ppg-integration`), including the persisted
  debt flag.
- Coverage impact: new service and controller code must stay at or above the
  JaCoCo ratchet floor (currently ~84% line / ~74% branch); no reduction is
  acceptable.

## Regression Strategy

- Existing tests affected: none expected — this adds a new endpoint and a new
  additive column; it does not change `PagoService.calcularSaldoPendiente`'s
  existing behavior or the legacy `archivarGestion` path.
- Full suite command: `mvn verify -pl backend-api`
- HTTP/Bruno API suite: `bash testing/scripts/test.sh`
- Legacy paths at risk: `ControllerNegocio.archivarGestion` /
  `GestionDeEscrituraJpaController.archivarGestiones` are left untouched and
  out of scope (see Non-Goals) — no regression risk there since nothing calls
  them today.

## Playwright Strategy

- Specs to add/update under `frontend/tests/e2e/`: a new spec covering the
  gestión-archiving action (the archiving UI itself does not exist yet and is
  part of this change's frontend surface).
- Golden path covered: archive a gestión with no pending debt — no warning
  shown, gestión moves to archived state.
- Edge / error paths covered: archive a gestión with pending debt — warning
  shown, user can cancel or confirm; confirming archives and persists the
  debt flag.
- Viewports: 320px (mobile) / 768px (tablet) / 1024px (desktop)
- Command: `cd frontend && npx playwright test`

## Deployment Strategy

- Flyway migration required: yes — `V15__add_deuda_pendiente_to_gestiones_de_escrituras.sql`
  adds an additive, nullable/defaulted `deuda_pendiente_al_archivar` boolean
  column to `gestiones_de_escrituras` (or an equivalent archiving-record
  table if design during implementation finds a better home — see Open
  Questions).
- Deployment order / coupling: migration and code ship together in the same
  release; the column has a default so no backfill step is required.
- Configuration or `.env` keys to add: none.
- Feature flag: no — additive, backward-compatible change.
- Smoke test after deploy (Gate 5): archive a test gestión with a known
  pending balance via the new endpoint and confirm the response and persisted
  record both reflect pending debt.

## Rollback Strategy

- Revert safe: yes — the new endpoint and column are purely additive; no
  existing endpoint or column is modified.
- Database rollback: none needed; the new column can remain unused if the
  code is rolled back (forward-fix only, per `.claude/rules/database-migrations.md`
  — never edit or drop an applied migration).
- Data written under the new behavior after revert: any gestión archived with
  the new endpoint keeps its `deuda_pendiente_al_archivar` value; harmless if
  the reverted code never reads it again.
- Blast radius if rollback is delayed: none — the feature is additive and
  does not alter existing archiving behavior for any caller that does not use
  the new endpoint.

## Open Questions

- Exact persistence target for the debt-at-archive flag (new column on
  `gestiones_de_escrituras` vs. a dedicated audit/history row) — does not
  change the spec or task breakdown, only the migration's shape; resolve
  during implementation by checking whether `registro_auditoria`
  (`AuditoriaAspect`) is a better fit than a new column.

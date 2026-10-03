> Governed by [CONSTITUTION.md](../../../CONSTITUTION.md) — §6 Quality Gates,
> §7 Testing Rules, §11 Release Rules.

## Context

#833 already writes History on complete-case create, `/transition`, and archive
via `ManagementBitacoraService.registerStatus`. Residual orphans live in
`ManagementController`: plain `create` / `update` and `updateCompleteCase` never
call the bitácora service. `getStatusActual` reads only History and 404s when
empty — common for legacy rows and any path that set status without History.

## Goals / Non-Goals

**Goals:**
- Close orphan status-write paths by reusing `ManagementBitacoraService`.
- Provide read-time entity-status fallback for `estado-actual`.
- Prove behavior with failing-then-green integration tests (TDD).
- Keep CU13 UI confirmation (TS-0028 / `useHistorial`) without rebuilding UI.

**Non-Goals:**
- Re-implement #833 paths or invent a second History writer.
- Rely on `AuditoriaAspect` / `registro_auditoria` for this table.
- Workflow-constrain plain PUT status changes (#804).
- Backfill History rows for pre-existing gestiones.
- Rename Spanish path segments (`/historial`, `/estado-actual`).

## Decisions

1. **Reuse `ManagementBitacoraService.registerStatus` only** — compare previous
   vs new status id in controller (or a tiny helper) before calling. Avoid a
   parallel writer or Aspect coupling.
2. **Register on first set and on change** — plain create with status → one row;
   create without status → no row; update/complete-case update only when status
   id changes.
3. **Read-time fallback for `estado-actual`** — synthesize `DtoHistorySummary`
   with `idHistory=null`, entity status, management id, `date=now`. Prefer this
   over a one-shot backfill migration (no schema/data rewrite risk).
4. **English in touched code** — translate Spanish comments/strings in files we
   touch; leave Spanish path segments until a rename ADR.

## Riesgos / Trade-offs

- [Synthesized summary has null History id] → Document in CU13; clients must
  treat `idHistory` as optional on this endpoint.
- [Double History if a future path calls registerStatus twice] → Call only after
  successful save and only when status id changes; do not call from Aspect.
- [Plain PUT still bypasses workflow validation] → Out of scope (#804); this
  change only audits what is already persisted.

## Testing Strategy

| Scenario (spec) | Test level | Test class / file |
|-----------------|------------|-------------------|
| Plain create with status writes initial History | integration | `ManagementHistorialOrphanWriteIntegrationTest` |
| Plain create without status writes no History | integration | same |
| Plain update that changes status writes History | integration | same |
| Plain update that keeps status writes no History | integration | same |
| Complete-case update that changes status writes History | integration | same |
| Complete-case update that keeps status writes no History | integration | same |
| estado-actual from History when rows exist | integration | same (or focused method) |
| estado-actual entity-status fallback when History empty | integration | same |
| estado-actual 404 when gestión missing | integration | same |
| estado-actual 404 when status null and History empty | integration | same |

- New unit tests: none required beyond existing `ManagementBitacoraServiceTest`
  unless a pure helper is extracted.
- New integration tests: `ManagementHistorialOrphanWriteIntegrationTest`
- Coverage impact: small controller branch coverage gain; ratchet floor held.

## Regression Strategy

- Existing tests affected: `ManagementBitacoraControllerIntegrationTest`
  (historial empty list still 200); `ManagementControllerIntegrationTest`
  (complete-case update may gain History — assert carefully); archive/transition
  suites unchanged.
- Full suite command: `mvn verify -pl backend-api`
- HTTP/Bruno API suite: update assertions if any expect 404 on empty estado-actual
- Legacy paths at risk: none (`jpa` unused for this)

## Playwright Strategy

- Specs to add/update: confirm `TS-0028-gestion-historial-feature.spec.ts` still
  matches product paths; no new UI work expected.
- Golden path covered: transition adds bitácora entry (existing)
- Edge / error paths covered: single initial entry (existing)
- Viewports: already covered in TS-0028
- Command: `cd frontend && npx playwright test TS-0028-gestion-historial-feature.spec.ts`
- If stack unavailable in VM: record confirmation that `useHistorial` + dialog
  remain wired; coordinator runs heavy Playwright via CI.

## Deployment Strategy

- Flyway migration required: no
- Deployment order / coupling: code-only deploy
- Configuration or `.env` keys to add: none
- Feature flag: no
- Smoke test after deploy (Gate 5): create gestión with status via plain POST;
  `GET /historial` has 1 row; clear History for a seeded case with status and
  confirm `estado-actual` returns 200

## Rollback Strategy

- Revert safe: yes — code-only; extra History rows written after deploy remain
  (harmless append-only audit)
- Database rollback: none needed
- Data written under the new behavior after revert: History rows stay; no schema
  dependency
- Blast radius if rollback is delayed: low (extra audit rows only)

## Migration Plan

No staged rollout. Read-time fallback covers pre-existing gestiones without
History backfill.

## Open Questions

None — scope fixed by issue #806 acceptance criteria and coordinator brief.

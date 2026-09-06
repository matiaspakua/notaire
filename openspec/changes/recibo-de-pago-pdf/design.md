> Governed by [CONSTITUTION.md](../../../CONSTITUTION.md) — §6 Quality Gates,
> §7 Testing Rules, §11 Release Rules.

## Context

`ReporteController`/`ReporteService` already generate PDFs for presupuestos,
historiales, testimonios and minutas. Two generation strategies coexist:
`generarPdfDesdeTemplate` (compiled `.jasper` templates) and
`generarPdfTextoSimple` (raw PDF content stream, no template asset, used by
`generarReporteCopiaTestimonio` and `generarReporteMinutaInscripcion`). `Pago`
has no printable representation at all. See proposal.md for the gap.

## Goals / Non-Goals

**Goals:**
- Add `GET /api/v1/reportes/recibo-pago/{idPago}` returning a PDF recibo.
- Reuse the existing raw-PDF helper; no new template asset, no new dependency.
- Wire the endpoint from the pagos screen (UI traceability).

**Non-Goals:**
- Physical printing or emailing the recibo.
- A graphically designed Jasper template (`.jrxml`/`.jasper`).

## Decisions

- **Raw PDF via `generarPdfTextoSimple`-style helper, not a new `.jasper`
  template.** A compiled Jasper template requires Jasper Studio and a binary
  build artifact this repo has no toolchain for; the existing raw-PDF path is
  already the established pattern for two other simple documents
  (`generarReporteCopiaTestimonio`, `generarReporteMinutaInscripcion`). Content
  needs 4+ lines (cliente, fecha, concepto(s), total) instead of the existing
  helper's fixed 4-line signature, so a new private
  `generarPdfReciboPago(Pago pago)` is added to `ReporteService` that builds
  its own content stream via the existing `buildPdf(String)` primitive,
  instead of overloading `generarPdfTextoSimple`.
- **Concepto(s) = nombres de los `Item` del presupuesto del pago**, fetched via
  `ItemRepository.findByFkIdPresupuestoIdPresupuesto`. If the presupuesto has
  no items, the recibo still prints cliente/fecha/total with an empty concepto
  line rather than failing — a pago without items is a pre-existing data state
  the recibo must not block on.
- **Total abonado = `Pago.getMonto()`**, not the presupuesto total — this is
  what makes a partial/installment payment's recibo scenario correct.
- **404 via `ResourceNotFoundException`**, consistent with
  `generarReporteCopiaTestimonio`/`generarReporteMinutaInscripcion`.

## Riesgos / Trade-offs

- Raw PDF has no logo/letterhead → acceptable per Out of Scope in proposal.md;
  matches existing simple reports.
- `Item.getNombre()` list could make the recibo grow past one page for a
  presupuesto with many ítems → out of scope for this change (existing helper
  has no pagination either); tracked as a follow-up only if it surfaces in
  practice.

## Testing Strategy

| Scenario (spec) | Test level | Test class / file |
|-----------------|------------|-------------------|
| Recibo de un pago simple | unit + integration | `ReporteServiceReciboPagoTest`, `ReporteControllerIntegrationTest` |
| Recibo de un pago parcial o en cuotas | unit | `ReporteServiceReciboPagoTest` |
| Recibo de un pago inexistente | unit + integration | `ReporteServiceReciboPagoTest`, `ReporteControllerIntegrationTest` |

- New unit tests (`src/test/java/.../unit/`): `ReporteServiceReciboPagoTest`
  (mocked `PagoRepository`/`ItemRepository`, asserts PDF bytes start with
  `%PDF-` and 404 exception path).
- New integration tests (`src/test/java/.../integration/`):
  `ReporteControllerIntegrationTest#shouldReturnPdfForRecibo` (H2, full
  `Pago`/`Presupuesto`/`Persona`/`Item` graph persisted, asserts
  `Content-Type: application/pdf` and 404 for unknown id).
- Coverage impact (JaCoCo ratchet floor; 80% target): net-new code fully
  covered by the tests above; no expected drop in the ratchet floor.

## Regression Strategy

- Existing tests affected: none — new endpoint, no change to existing
  `ReporteController`/`ReporteService` methods.
- Full suite command: `mvn verify -pl backend-api`
- HTTP/Bruno API suite: `bash testing/scripts/test.sh` (also add a Bruno
  request under `backend-api/api-test/pagos/` per `COVERAGE.md`).
- Legacy paths at risk (e.g. `jpa` package, `frontend-swing`): none — `jpa`
  package untouched, `frontend-swing` removed per CLAUDE.md.

## Playwright Strategy

- Specs to add/update under `frontend/tests/e2e/`: extend the existing
  cobranza/pagos E2E spec (or add `TS-00xx-recibo-pago.spec.ts` if none
  covers the pagos screen action area) to click "Emitir recibo" after
  creating a pago and assert the download/PDF response.
- Golden path covered: crear pago → emitir recibo → PDF descargado.
- Edge / error paths covered: botón deshabilitado o error visible si el pago
  aún no fue guardado.
- Viewports: 320px (mobile) / 768px (tablet) / 1024px (desktop)
- Command: `cd frontend && npx playwright test`

## Deployment Strategy

- Flyway migration required: no
- Deployment order / coupling: none — additive endpoint, backend-only + one
  frontend button.
- Configuration or `.env` keys to add: none.
- Feature flag: no
- Smoke test after deploy (Gate 5): `GET /api/v1/reportes/recibo-pago/{idPago}`
  against a known pago on the deployed environment returns `200` with
  `Content-Type: application/pdf`.

## Rollback Strategy

- Revert safe: yes — additive endpoint and UI button, no schema change, no
  contract change to existing endpoints.
- Database rollback: none needed.
- Data written under the new behavior after revert: none (read-only report
  generation, nothing persisted).
- Blast radius if rollback is delayed: none beyond the recibo feature being
  unavailable.

## Open Questions

None.

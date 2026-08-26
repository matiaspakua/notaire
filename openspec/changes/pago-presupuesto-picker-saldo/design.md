> Governed by [CONSTITUTION.md](../../../CONSTITUTION.md) — §6 Quality Gates,
> §7 Testing Rules, §11 Release Rules.

## Context

`frontend/src/app/dashboard/pagos/page.tsx` currently has "Presupuesto ID" as
a plain `<Input type="number">` inside the `FormContainer > FormSection >
FormField` form used to create/edit a `Pago`. No list, no client name, no
saldo. The data needed already exists behind two hooks with no backend
change required:

- `usePresupuestos()` (`frontend/src/hooks/usePresupuestos.ts`) — paginated
  list of all `Presupuesto`, each including its `persona` relation.
- `usePresupuestoResumen(id)` (same file) — wraps
  `GET /api/v1/presupuestos/{id}/resumen`, already returns `saldoPendiente`.

`dashboard/gestiones/page.tsx` already has the precedent for a
presupuesto-picking `<Select>` fed by `usePresupuestos()` (see
`proposal.md` — Architecture review). See `proposal.md` — Objetivo for why
this exists.

## Goals / Non-Goals

**Goals:**
- Replace the numeric ID `<Input>` with a `<Select>` picker listing
  presupuesto number + client name.
- Show the selected presupuesto's saldo pendiente in the form before submit.
- Reuse existing hooks/endpoints; zero backend changes.

**Non-Goals:**
- A free-text-search Combobox — no such primitive exists in the design
  system yet (see `proposal.md` — Out of Scope).
- Searching by client identification document — no backend endpoint
  supports it today (see `proposal.md` — Out of Scope).
- Changing `PagoService`/`PagoController` or any validation of `monto`
  against saldo pendiente — that's `pago-limite-saldo-pendiente` (#848).

## Decisions

- **Reuse `<Select>`/`<SelectTrigger>`/`<SelectContent>`/`<SelectItem>`
  (shadcn) instead of building a Combobox.** Alternative considered: add a
  `cmdk`-based Combobox for real text filtering. Rejected for this change —
  it is a new design-system primitive with its own dependency and pattern
  to establish; `gestiones/page.tsx` already proves `<Select>` is the
  established pattern for this exact "pick a presupuesto" interaction, and
  Radix's `<Select>` already supports keyboard typeahead. Introducing a new
  primitive is a separate, larger UI decision, not a prerequisite for
  showing the saldo pendiente.
- **Reuse `usePresupuestoResumen` for the saldo pendiente display, not
  `GET /pagos/presupuesto/{id}/saldo`.** Alternative considered: call the
  `saldo`-only endpoint directly. Rejected — `usePresupuestoResumen` is
  already wired for CU47 and returns the same `saldoPendiente` plus context
  (número de gestión, encabezado) that can be shown alongside it without a
  second round trip or a second hook to maintain.
- **List all presupuestos via `usePresupuestos()`, filtered client-side by
  the picker's own typeahead, instead of adding a backend search
  endpoint.** Alternative considered: add
  `GET /presupuestos/buscar?q=...` searching by persona name/número.
  Rejected for this change — `usePresupuestos()` already returns the full
  paginated list used elsewhere (`gestiones/page.tsx`); a dedicated search
  endpoint is only justified if the list becomes too large to browse, which
  is a separate performance concern, not part of this Issue's Acceptance
  Criteria.

## Riesgos / Trade-offs

- [Presupuesto list grows large enough that browsing a `<Select>` becomes
  impractical] → Mitigation: none in this change; flagged in Non-Goals. If
  it becomes a real problem, a follow-up issue should add a search endpoint
  and/or a Combobox — do not solve it speculatively here (YAGNI).
- [`usePresupuestoResumen` fails (network/server error) after a presupuesto
  is selected] → Mitigation: spec scenario "Saldo pendiente fails to load"
  requires the form to show an explicit unavailable-state instead of a
  stale or blank value; covered by a unit test (see Testing Strategy).
- [Picker lists presupuestos regardless of `estado`, including ones already
  fully paid] → Mitigation: none — CU15 pasos 2-3 already scope the search
  to "presupuestos pendientes asociados"; `GET /presupuestos` today returns
  all, so the picker may show more than CU15 implies. Documented and
  accepted for this change since filtering by `estado` client-side without
  a canonical "pendiente" definition risks hiding a presupuesto the
  operator legitimately needs; flagged as a follow-up if it proves
  confusing in practice.

## Testing Strategy

| Scenario (spec) | Test level | Test class / file |
|-----------------|------------|-------------------|
| Operator selects a presupuesto from the picker | E2E | `frontend/tests/e2e/cu15-pagos.spec.ts` |
| Operator picks a presupuesto and it becomes the payment target | E2E | `frontend/tests/e2e/cu15-pagos.spec.ts` |
| No presupuestos available | unit | `frontend/src/tests/unit/pagos-page.test.tsx` |
| Saldo pendiente is shown after selecting a presupuesto | E2E | `frontend/tests/e2e/cu15-pagos.spec.ts` |
| Saldo pendiente updates when the selection changes | E2E | `frontend/tests/e2e/cu15-pagos.spec.ts` |
| Saldo pendiente fails to load | unit | `frontend/src/tests/unit/pagos-page.test.tsx` |

- New unit tests (`frontend/src/tests/unit/pagos-page.test.tsx`, new file
  following the `login-page.test.tsx` pattern): empty-picker state, and the
  saldo pendiente error state when `usePresupuestoResumen` returns an
  error.
- New integration tests: none — no backend change.
- Coverage impact (JaCoCo ratchet floor; 80% target): none — this change
  does not touch `backend-api`, so it does not affect JaCoCo. Frontend
  coverage (`vitest run --coverage`) gains the new unit test file.

## Regression Strategy

- Existing tests affected: `frontend/tests/e2e/cu15-pagos.spec.ts` — its
  existing cases that create/edit a payment via the numeric ID input must
  be updated to use the new picker instead (the input itself is removed).
- Full suite command: `mvn verify -pl backend-api` (unaffected — no backend
  change, run only to confirm no regression).
- HTTP/Bruno API suite: `bash testing/scripts/test.sh` (unaffected — no
  endpoint changes).
- Legacy paths at risk (e.g. `jpa` package, `frontend-swing`): none —
  `frontend-swing` is removed; `jpa` package not touched.

## Playwright Strategy

- Specs to add/update under `frontend/tests/e2e/`: `cu15-pagos.spec.ts` —
  update existing payment-creation cases to use the picker
  (`data-testid="select-presupuesto-pago"`, matching the
  `select-presupuesto-gestion` convention from `gestiones/page.tsx`), add a
  case asserting the saldo pendiente is displayed and updates when the
  selection changes.
- Golden path covered: select a presupuesto from the picker, see its saldo
  pendiente, register a payment.
- Edge / error paths covered: empty picker (no presupuestos), saldo
  pendiente fetch failure shown as unavailable rather than blank/stale.
- Viewports: 320px (mobile) / 768px (tablet) / 1024px (desktop)
- Command: `cd frontend && npx playwright test cu15-pagos`

## Deployment Strategy

- Flyway migration required: no
- Deployment order / coupling: none — frontend-only change, deploys
  independently.
- Configuration or `.env` keys to add: none.
- Feature flag: no
- Smoke test after deploy (Gate 5): open the payment form, confirm the
  presupuesto picker lists real presupuestos and selecting one shows its
  current saldo pendiente.

## Rollback Strategy

- Revert safe: yes — frontend-only, no schema or API contract change to
  unwind.
- Database rollback: none needed.
- Data written under the new behavior after revert: none — this change
  does not alter how a `Pago` is persisted, only how its target
  presupuesto is selected in the UI.
- Blast radius if rollback is delayed: none beyond the UI itself reverting
  to the numeric-ID input.

## Open Questions

None — see Riesgos / Trade-offs for the accepted (not deferred) limitation
around unfiltered `estado` in the picker list.

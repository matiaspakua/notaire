> Governed by [CONSTITUTION.md](../../../CONSTITUTION.md) — §6 Quality Gates,
> §7 Testing Rules, §11 Release Rules.

## Context

`PagoService.procesarPago(...)` already computes `saldoPendiente` (via the
existing, reusable `calcularSaldoPendiente(idPresupuesto)`) but only logs it —
it is never compared against `monto`. The only existing guard is `monto <= 0`
(`IllegalArgumentException` → `PagoController` catches it locally and returns
`400`).

`PagoController.procesarPago` (`POST /pagos`) and `procesarPagoParams`
(`POST /pagos/params`) each wrap the service call in a local
`try { ... } catch (IllegalArgumentException e) { 400 } catch (Exception e) { 500 }`.
This is a **local** try/catch per method, not `@ExceptionHandler` dispatch —
`GlobalExceptionHandler` (`@RestControllerAdvice`) never sees exceptions
thrown from these two methods unless they escape the method's own catch
blocks, and today's blanket `catch (Exception e)` would turn any new
unchecked exception into a `500`, not the `409` the `@ApiResponses` on
`POST /pagos` already (falsely) advertises. See proposal.md - Objetivo.

## Goals / Non-Goals

**Goals:**
- Make `PagoController.procesarPago` / `procesarPagoParams` actually return
  the `409` they already document, only for the saldo-exceeded case.
- Keep the validation itself inside `PagoService` (business rule, not a
  controller concern), reusing `calcularSaldoPendiente`.

**Non-Goals:**
- Routing this exception through `GlobalExceptionHandler`. Not chosen because
  these two controller methods have local try/catch blocks that would
  intercept any `RuntimeException` first (see Context) — adding a new
  `@ExceptionHandler` case would be dead code unless the local catches are
  also removed, which is a broader refactor of unrelated existing error
  handling (`404`/`500` paths) outside this change's scope.
- Changing `editarPago` — see proposal.md - Out of Scope.

## Decisions

**Decision: new checked-by-convention business exception,
`SaldoPendienteExcedidoException extends RuntimeException`, thrown by
`PagoService.procesarPago`; `PagoController` adds an explicit `catch` for it
before the generic `catch (Exception e)`, returning `409 Conflict` with no
body (`ResponseEntity.status(HttpStatus.CONFLICT).build()`).**
- Alternative considered: extend `NotaireException`/`BusinessValidationException`
  (`exception` package) and let `GlobalExceptionHandler` map it. Rejected —
  per Context, these two controller methods swallow it into a `500` via their
  own `catch (Exception e)` before it can reach the `@RestControllerAdvice`,
  so this would silently produce the wrong status code. Using `NotaireException`
  would also carry a body-shape (`ErrorResponse`) the controller does not
  currently emit for its other `400`/`404` branches on this endpoint (they all
  return `.build()` with no body), which would be an inconsistent surface.
- No response body (`.build()`, not `ErrorResponse`): consistent with the
  existing `400`/`404` branches in `PagoController` for this same endpoint —
  the frontend already keys off the HTTP status code (`ApiError.status`), not
  an error body, to select its message (see proposal.md - What Changes).

**Decision: validate inside `procesarPago`, right after the existing
`monto <= 0` check, using the existing `calcularSaldoPendiente(idPresupuesto)`
— no new saldo-calculation logic.**
- Alternative considered: duplicate the saldo calculation inline (as the
  unused local variables in `procesarPago` currently do) instead of calling
  `calcularSaldoPendiente`. Rejected — `procesarPago` already computes
  `totalPresupuesto`/`totalPagado`/`saldoPendiente` inline for logging only;
  reusing `calcularSaldoPendiente` instead of keeping two calculations in sync
  removes duplication (DRY) rather than adding to it.

**Decision: frontend distinguishes the `409` by HTTP status only
(`error instanceof ApiError && error.status === 409`), not by parsing a
response body, and shows a fixed, translated message.**
- Alternative considered: have the backend return a structured body
  (`ErrorResponse`) with a machine-readable reason so the frontend could
  handle multiple `409` causes distinctly. Rejected as unnecessary now —
  `POST /pagos` has exactly one `409` cause after this change; adding a body
  contract for a single case is speculative (YAGNI) and can be added later
  without breaking the frontend, since status-code branching is
  additive-compatible.

## Riesgos / Trade-offs

- [No response body on `409`] → the frontend cannot show a backend-computed
  number (e.g. "exceeds by $X"); it shows a fixed message instead. Mitigation:
  acceptable per proposal.md scope; if the business later wants the exact
  overage amount surfaced, that is a new, explicit change (add a body to this
  one `409` path), not a silent scope creep here.
- [Breaking change on `POST /pagos` / `POST /pagos/params` response code] →
  any existing caller that assumed "any `monto` is accepted" will start
  seeing `409` instead of `201`. Mitigation: proposal.md already flags this
  as an intentional, justified bug fix (§Surface area); no external API
  consumers are known outside this monorepo's own frontend, which this change
  updates in the same PR.
- [Two near-identical controller methods (`procesarPago`, `procesarPagoParams`)
  need the same new `catch` block] → risk of the two drifting if one is
  updated and not the other later. Mitigation: out of scope to deduplicate
  them now (pre-existing duplication, not introduced by this change); both
  are covered by dedicated integration tests so drift would fail CI.

## Testing Strategy

| Scenario (spec) | Test level | Test class / file |
|-----------------|------------|-------------------|
| Payment within the saldo pendiente is accepted | unit | `PagoServiceTest` |
| Payment exactly matching the saldo pendiente is accepted | unit | `PagoServiceTest` |
| Payment exceeding the saldo pendiente is rejected | unit + integration | `PagoServiceTest` (throws `SaldoPendienteExcedidoException`); `PagoControllerTest` (HTTP 409 for both `POST /pagos` and `POST /pagos/params`) |
| Saldo pendiente calculation already accounts for prior payments | unit | `PagoServiceTest` (arrange a presupuesto with an existing payment, assert the new payment is validated against the reduced saldo) |
| Operator sees a specific message when a payment is rejected for exceeding saldo | E2E | `frontend/tests/e2e/cu15-pagos.spec.ts` (existing CU15 spec file — add a case) |

- New unit tests (`src/test/java/.../unit/`): additions to `PagoServiceTest`
  (saldo-exceeded rejected, exact-match accepted, prior-payments-reduce-saldo)
  and to `PagoControllerTest` (409 mapping for both POST endpoints).
- New integration tests (`src/test/java/.../integration/`): additions to
  `PagoIntegrationTest` (or `PagoServiceIntegrationTest`, whichever already
  exercises `procesarPago` end-to-end against PostgreSQL) covering the
  saldo-exceeded HTTP 409 path.
- TDD: all of the above are written first and run (`mvn test -pl backend-api
  -Dtest=PagoServiceTest,PagoControllerTest`) to confirm they FAIL before
  `SaldoPendienteExcedidoException` and the controller `catch` exist.
- Coverage impact: adds branches to an already-covered method
  (`procesarPago`) and a new small exception class; expected to hold or raise
  the JaCoCo ratchet floor, not lower it (`.claude/rules/code-quality.md`).

## Regression Strategy

- Existing tests affected: `PagoServiceTest` and `PagoControllerTest` already
  cover the accepted-payment and `monto <= 0` paths for `procesarPago` /
  `procesarPagoParams` — these must keep passing unchanged, since this change
  only adds a new rejection branch above the existing floor check, it does
  not alter the `monto <= 0` behavior.
- Full suite command: `mvn verify -pl backend-api`
- HTTP/Bruno API suite: `bash testing/scripts/test.sh`
- Legacy paths at risk: none — `jpa` package and `frontend-swing` do not
  touch `PagoService`/`PagoController`.

## Playwright Strategy

- Specs to add/update under `frontend/tests/e2e/`: `cu15-pagos.spec.ts`
  (existing CU15 spec) — add a case that submits a `monto` greater than the
  presupuesto's saldo pendiente and asserts the specific rejection message
  is shown instead of the generic save-error toast.
- Golden path covered: existing `cu15-pagos.spec.ts` cases (payment within
  saldo accepted) are unaffected and continue to pass.
- Edge / error paths covered: new case — submit an amount exceeding saldo
  pendiente, assert HTTP 409 is surfaced as the specific message, not
  `errorSave`.
- Viewports: 320px (mobile) / 768px (tablet) / 1024px (desktop) — reuse the
  existing spec's viewport matrix; no new responsive surface is introduced
  (same form, new error branch).
- Command: `cd frontend && npx playwright test cu15-pagos`

## Deployment Strategy

- Flyway migration required: no — no schema change.
- Deployment order / coupling: backend and frontend changes ship together
  (same PR); no ordering constraint since the frontend change only adds a
  new `409` branch alongside the existing generic error handling — an
  intermediate state (old frontend + new backend) would just show the
  generic `errorSave` message instead of the specific one, not break.
- Configuration or `.env` keys to add: none.
- Feature flag: no.
- Smoke test after deploy (Gate 5): `POST /api/v1/pagos` with a `monto`
  greater than an existing presupuesto's saldo pendiente returns `409`; a
  `monto` within saldo still returns `201` — both checked manually or via
  `testing/scripts/test.sh` (Bruno) against the deployed environment.

## Rollback Strategy

- Revert safe: yes — reverting removes the new validation and the app
  returns to today's (buggy) always-accept behavior; no data migration was
  introduced, so a code-only revert is sufficient.
- Database rollback: none needed — no schema change.
- Data written under the new behavior after revert: payments that were
  correctly rejected while this change was live simply become acceptable
  again after revert (matches today's pre-change behavior) — no orphaned or
  inconsistent data is created either way.
- Blast radius if rollback is delayed: none beyond the bug this change fixes
  — a delayed rollback just means the (intentional) stricter validation
  keeps rejecting over-saldo payments, which is the desired end state, not a
  new failure mode.

## Open Questions

None — proposal.md - Out of Scope already resolves the two candidate
ambiguities (reject vs. warn-and-allow; whether `editarPago` is included) as
explicit scope decisions, not deferred unknowns.

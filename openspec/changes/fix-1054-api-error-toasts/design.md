> Governed by [CONSTITUTION.md](../../../CONSTITUTION.md) — §6 Quality Gates,
> §7 Testing Rules, §11 Release Rules.

## Context

Audit #1054 (and prep `internal/next-issue-after-1052.md`): 15 of ~29 toasting
pages never call `extractApiError`. Verified pages still using generic toasts:

- `administracion/usuarios`, `administracion/roles`, `administracion/workflows`,
  `administracion/workflows/[id]`, `administracion/plantillas`,
  `administracion/items`, `items`, `presupuestos`, `pagos`, `copias`,
  `inmuebles`, `suplencias`, `protocolo`, `documentos`, `reportes`

`frontend/src/lib/utils.ts` `extractApiError` returns null unless the
`ApiError.message` string contains `[400]` or `[409]`. `apiDelete` already
routes through `rejectApiFailure` → `ApiError` after #1053; remaining gap is
status coverage in helpers + page adoption. `FormField` already accepts
`error?: string` but no page sets it from API field detail. Backend
`GlobalExceptionHandler` joins bean-validation fields as
`"field: message; …"` inside `ErrorResponse.message` (no separate map).
Related: #945 (Persona), #615 (E2E validation coverage gap), #1053 (401).

## Goals / Non-Goals

**Goals:**

- One shared mutation-error presentation path for toasts + optional field errors.
- Map `ApiError` bodies for business-relevant statuses (at least 400, 404, 409,
  422, 500 with parseable JSON `message`/`error`); never swallow server text
  when present.
- All 15 listed pages use the handler; inline `FormField` / `aria-invalid` when
  a field name can be parsed from the API message.
- Unit tests first; Playwright smoke for representative CRUD (CU15 / CU20).

**Non-Goals:**

- Session 401 UX (#1053), admin guards (#1052), cookie auth (#1051).
- Rewriting backend `ErrorResponse` to a structured `fieldErrors` object
  (unless a tiny additive change becomes necessary and is scoped in implement).
- Full #615 “one E2E per major form” matrix.
- Translating or localizing server messages client-side.

## Decisions

1. **Central shared handler, not 15 one-off `extractApiError` copies**
   - Why: same failure mode as pre-#1053 page-level 401 handling; AC requires
     one mapping for all statuses.
   - Shape (illustrative): `presentMutationError(err, { fallback, setFieldErrors? })`
     → toast with `extractApiError`-class message or fallback; optionally
     returns `{ fieldErrors }` for form state.
   - Keep #945 Persona behavior as the reference consumer.

2. **Widen message extraction beyond 400/409**
   - Why: deletes/conflicts often return 404/409/500 with useful `message`;
     current helper drops them → generic toast.
   - Keep excluding authenticated 401 from mutation toasts (session-expiry
     handler owns that path).
   - Prefer reading `ApiError.status` + `ApiError.body` when `err instanceof
     ApiError` instead of regex on `err.message` (more reliable).

3. **Field errors from joined `field: msg` strings**
   - Why: backend today does not emit a JSON map; parsing the joined message is
     enough for Gate 1 AC without a BE migration.
   - When no field can be mapped, toast-only (still shows full message).
   - Set `aria-invalid` on the control or rely on `FormField` error rendering
     already associated with the label/control pattern.

4. **E2E: one representative pago + one usuario (or roles) happy-error path**
   - Why: prep risk note — avoid 15 flaky suites; satisfy #1054 AC and reduce
     #615 gap without closing the whole matrix.
   - Allocate next free TS id at implement time (TS-0095; TS-0094 taken by #1052).


5. **`apiDelete` ApiError**
   - Why: issue AC still lists it; #1053 already throws `ApiError` via
     `rejectApiFailure`. Implement task = verify + extend unit tests for
     400/409 business bodies on DELETE; no second code path.

## Riesgos / Trade-offs

| Risk | Mitigation |
|------|------------|
| Broad 15-page diff / merge conflicts with #1137 | Prep only now; implement after #1137 merges; centralize helper first |
| Playwright flaky on forced API errors | Route intercept / stub API 4xx with known `ErrorResponse` body |
| Field-name parse false positives | Only map fields present on the active form; else toast-only |
| Widening extractApiError breaks tests that expect null for 404 | Update unit expectations deliberately; document in regression strategy |
| Overlap with pages that already call extractApiError | Leave working pages alone unless they need the shared handler for field errors |

## Testing Strategy

| Scenario (spec) | Test level | Test class / file |
|-----------------|------------|-------------------|
| ApiError body message shown for 400/409/404 | unit | `frontend/src/tests/unit/utils.test.ts` and/or new `mutation-error.test.ts` |
| apiDelete rejects with ApiError including parseable body | unit | `frontend/src/tests/unit/api-client.test.ts` |
| Shared handler toasts message then fallback | unit | `mutation-error.test.ts` |
| Field detail → fieldErrors map | unit | `mutation-error.test.ts` |
| Authenticated 401 not toasted as mutation error | unit | handler + existing session-expiry tests |
| Representative CRUD shows server message in UI | E2E | `frontend/tests/e2e/TS-0094-api-error-toasts.spec.ts` (id confirm at implement) |
| Inline field error when stub returns `nombre: …` | E2E | same spec (usuarios or roles) |

- New unit tests (`frontend/src/tests/unit/`): extraction + handler + apiDelete body
- New integration tests (`src/test/java/.../integration/`): n/a — frontend-only
- Coverage impact (JaCoCo ratchet floor; 80% target): unchanged (no backend code)

## Regression Strategy

- Existing tests affected: `utils.test.ts` (404 null expectation), pages that
  already use `extractApiError` (behavior should stay or improve), `api-client`
  delete tests, Persona (#945) flows
- Full suite command: `mvn verify -pl backend-api` (sanity; no backend delta)
- Frontend: `cd frontend && npm test` + Playwright focused + full e2e in CI
- HTTP/Bruno API suite: n/a (no API contract change)
- Legacy paths at risk: none

## Playwright Strategy

- Specs to add/update under `frontend/tests/e2e/`:
  - New `TS-0094-api-error-toasts.spec.ts` (confirm free id; map in
    `E2E-TEST-MAPPING.md`; link CU15, CU20, #1054, #615)
- Golden path: mutate with stubbed 400/409 business body → toast (or field
  error) shows server `message`, not only generic i18n fallback
- Edge / error paths: fallback when body empty; 401 still redirects via #1053
- Viewports: 320px / 768px / 1024px for toast/field visibility on one form
- Command: `cd frontend && npx playwright test TS-0094-api-error-toasts`
- Does **not** close #615 unless the suite clearly satisfies that issue’s full
  AC; prefer leaving #615 open with a note if residual forms remain

## Deployment Strategy

- Flyway migration required: no
- Deployment order / coupling: frontend-only deploy
- Configuration or `.env` keys to add: none
- Feature flag: no
- Smoke test after deploy (Gate 5): force a known business validation (e.g.
  duplicate role/user or overpayment) and confirm toast shows server text

## Rollback Strategy

- Revert safe: yes — pure client UX; revert the PR
- Database rollback: none needed
- Data written under the new behavior after revert: none
- Blast radius if rollback is delayed: users again see generic mutation toasts

## Migration Plan

None beyond shipping the frontend change after Gate 3/4. Copy this draft from
`/cursor/stores/self/internal/openspec-1054/` into
`openspec/changes/fix-1054-api-error-toasts/` when implement starts.

## Open Questions

None blocking Gate 1. Implement may choose helper module path
(`lib/mutation-error.ts` vs extend `utils.ts`); either is fine if unit tests
own the contract.

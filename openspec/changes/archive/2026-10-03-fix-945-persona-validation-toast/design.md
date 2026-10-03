> Governed by [CONSTITUTION.md](../../../CONSTITUTION.md) — §6 Quality Gates,
> §7 Testing Rules, §11 Release Rules.

## Context

Issue #945 (bug, FRONTEND, TEST, priority:medium). Use Cases CU17 / CU61.
Researched on `origin/main` tip `6b246a72` (2026-10-03):

| Finding | Detail |
|---------|--------|
| `personas/page.tsx` `handleSaveError` | Already calls `toast.error(extractApiError(err) ?? t("errorSave"))` for non-409; 409 keeps localized `duplicateDocument` |
| `extractApiError` / `presentMutationError` | Generalized in #1054 (`mutation-error.ts`); Personas still uses local handler |
| DNI field | `FormField` for DNI is **not** `required`; empty DNI can reach the API |
| Dedup-EDGE | Exists in `TS-0015-personas-clientes-workflow.spec.ts`; issue AC still unchecked for pass |
| Toast matchers | Expect `/requerido\|obligatorio\|blank/i` (dialog) or `/dni\|identificacion\|requerido\|obligatorio\|blank/i` (toast) |

Queue ahead (do not implement until clear): `#1040 → #1043 → #1045 → #1056 →
\#1055 → #1050 → #1058 → #976`.

## Goals / Non-Goals

**Goals:**

- Dedup-EDGE green: empty identification → visible validation feedback; dialog
  stays open.
- Non-409 `ApiError` never shows only generic `errorSave` when a body message
  is parseable.
- Preserve curated 409 duplicate UX.

**Non-Goals:**

- Re-migrating all CRUD pages (#1054 done).
- Backend message localization / changing validation rules.
- New Persona fields or duplicate-detection logic.

## Decisions

1. **Treat code path as mostly landed; prove with E2E**
   - On `6b246a72` the non-409 `extractApiError` call is already present.
   - Remaining work is make Dedup-EDGE reliably pass and close any residual
     gap (e.g. message text not matching assertions, race, or dialog closing).

2. **Prefer toast/form surfacing of server message over inventing client-only
   required DNI**
   - Only add client `required` / inline FormField error if server path cannot
     satisfy Dedup-EDGE without flakiness; document the choice in the PR.

3. **Optional `presentMutationError` adoption**
   - Allowed if 409 path still uses localized copy (`preferFallback` or keep
     dedicated 409 branch). Do not regress English leak on 409.

4. **No backend change**
   - Bean-validation bodies are sufficient; no Flyway / DTO contract change.

## Riesgos / Trade-offs

- [E2E flaky toast timing] → Use existing sonner locators + adequate timeout;
  assert dialog remains open as hard signal.
- [API message field name differs (`identificationNumber` vs Spanish)] → Toast
  regex already includes `blank`; extend matcher only if real body lacks it.
- [Regression of 409 UX] → Keep dedicated 409 branch; add/keep unit or E2E
  duplicate path if touched.
- [Queue collision with Playwright-heavy PRs] → Serialize per heavy-ci-merge
  workflow; draft until gate green.

## Testing Strategy

| Scenario (spec) | Test level | Test class / file |
|-----------------|------------|-------------------|
| Non-409 validation message toasted | unit + E2E | unit: handler/`extractApiError` if refactored; E2E Dedup-EDGE |
| Fallback when body empty | unit | `utils` / `mutation-error` / local handler test |
| 409 keeps localized duplicate toast | unit or existing E2E | Personas duplicate path / existing suite |
| Dialog stays open on validation failure | E2E | Dedup-EDGE |
| Dedup-EDGE passes in CI | E2E | `TS-0015-personas-clientes-workflow.spec.ts` |

- New unit tests (`src/test/java/.../unit/`): n/a (frontend)
- Frontend unit: add/adjust only if handler is refactored to
  `presentMutationError`
- Coverage impact (JaCoCo): none expected

TDD: run Dedup-EDGE against current tree first and observe failure/flake mode;
then fix the minimal UI/assertion path; re-run until green.

## Regression Strategy

- Existing tests affected: `TS-0015-personas-clientes-workflow.spec.ts`; any
  Persona unit tests for save errors; #1054 `mutation-error.test.ts` if shared
  helper is used.
- Full suite command: `cd frontend && npm test`; `mvn verify -pl backend-api`
  (sanity).
- HTTP/Bruno: n/a (no API change).
- Legacy paths at risk: do not touch `jpa` or recreate Swing.

## Playwright Strategy

- Specs to add/update: `frontend/tests/e2e/TS-0015-personas-clientes-workflow.spec.ts`
  (`Dedup-EDGE` — stabilize / fix under test, do not delete).
- Golden path: create persona with valid DNI still succeeds (existing tests).
- Edge / error: empty DNI → validation feedback + dialog open.
- Viewports: prove at least one of 768/1024 for Dedup-EDGE; smoke 320 if
  dialog layout differs.
- Command: `cd frontend && npx playwright test TS-0015-personas-clientes-workflow`

## Deployment Strategy

- Flyway migration required: no
- Deployment order / coupling: frontend-only; merge after heavy CI green
- Configuration or `.env` keys: none
- Feature flag: no
- Smoke test after deploy (Gate 5): manually create persona without DNI → see
  specific error; with valid data → create succeeds

## Rollback Strategy

- Revert safe: yes (frontend-only toast/error presentation)
- Database rollback: none needed
- Data written under the new behavior after revert: none
- Blast radius if rollback is delayed: users briefly see generic or specific
  messages — low risk

## Migration Plan

1. Reproduce Dedup-EDGE on updated `main`.
2. Minimal fix to error presentation and/or assertion alignment.
3. Green Playwright + unit; docs + CHANGELOG; PR.

## Open Questions

None material. Assume server 400 body remains parseable JSON with a message
containing a blank/required signal; if implement-time bodies differ, extend
toast matcher rather than inventing parallel client validation unless E2E
cannot be made reliable.

> Governed by [CONSTITUTION.md](../../../../CONSTITUTION.md) — §6 Quality Gates,
> §7 Testing Rules, §11 Release Rules.

## Context

Login already authenticates via `authUseCases` (ADR-030). Visual markup uses ad-hoc
`Card`/`Label`/`Input` with uppercase labels. Theme primitives and motion tokens
already exist (`form-patterns.tsx`, `motion.ts`).

## Goals / Non-Goals

**Goals:**
- Compose login credentials with `FormContainer` / `FormField` / `FormActions`.
- Preserve CU84 behaviour and existing `data-testid`s used by Playwright.
- Align label typography with sentence case (design-system restraint).

**Non-Goals:**
- Redesign the whole product shell or brand mark.
- Change auth API contracts or session cookies.
- Introduce GSAP / Lenis / 3D (CRUD auth surface; utilitarian motion only).

## Decisions

1. **Keep brand hero outside FormHeader** — `FormHeader` renders an `h1`; the page
   already uses `h1` for "Notaire". Use a quiet brand block + `FormContainer` for
   fields so heading hierarchy stays valid.
2. **No FormSection border** — FormSection adds a top border suited to dialogs;
   login uses FormContainer + FormField + FormActions only for a single-task surface.
3. **Reuse Input from ui/** — FormField clones/context-wires Input; keep
   `data-testid` on inputs and submit button for E2E stability.
4. **Motion** — keep the existing icon spring; no staggered field entrances (AI-slop).

## Riesgos / Trade-offs

- [Risk] Playwright selectors break → Mitigation: keep `input-username`,
  `input-password`, `btn-sign-in`, `session-expired-message`.
- [Risk] FormField label styling differs from old uppercase → Mitigation: intentional;
  covered by design-system doc update.
- [Risk] Double `h1` if FormHeader used → Mitigation: decision 1.

## Testing Strategy

| Scenario (spec) | Test level | Test class / file |
|-----------------|------------|-------------------|
| Login fields are FormField-wrapped | unit | `frontend/src/tests/unit/login-page.test.tsx` |
| Submit lives in FormActions | unit | `frontend/src/tests/unit/login-page.test.tsx` |
| Existing auth behaviours hold | unit | existing cases in same file |
| Golden login path still works | E2E | existing login helpers (TS-0094 / TS-0154) — no new TS unless regression |

- New unit tests: pattern-contract assertions (source or DOM markers).
- New integration tests: n/a (frontend-only).
- Coverage impact: frontend Vitest only; JaCoCo unchanged.

## Regression Strategy

- Existing tests affected: `login-page.test.tsx` (extend, do not weaken).
- Full suite command: `bash frontend/verify.sh`; backend unchanged.
- HTTP/Bruno API suite: n/a for this change.
- Legacy paths at risk: none.

## Playwright Strategy

- Specs to add/update: none required if testids preserved; run a representative
  login-dependent smoke (`TS-0094` or suite subset) against the branch build.
- Golden path: valid credentials → dashboard (existing helpers).
- Edge / error: session expired banner remains.
- Viewports: login already centered; spot-check 320 / 768 / 1024.
- Command: `cd testing/e2e && npx playwright test TS-0094-admin-route-guard.spec.ts`

## Deployment Strategy

- Flyway migration required: no
- Deployment order / coupling: frontend only
- Configuration or `.env` keys: none
- Feature flag: no
- Smoke test after deploy (Gate 5): open `/login`, submit valid credentials

## Rollback Strategy

- Safe git revert of the login page commit; no schema or cookie change.

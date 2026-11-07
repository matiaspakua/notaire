> Governed by [CONSTITUTION.md](../../../../CONSTITUTION.md) — §6 Quality Gates,
> §7 Testing Rules, §11 Release Rules.

## Context

Login already uses FormContainer, FormField, and FormActions (#611) and
MotionConfig `reducedMotion="user"` (#1368). The seal still runs a spring with
`rotate: -8`. The lines "Notaire", the subtitle, and "Iniciar sesión" sit in
one stack on the page background, while dashboard cards use a 28px radius and
`apple-shadow`.

## Goals / Non-Goals

**Goals:**

- One white desk card for the sign-in task.
- Heading levels that match the reading order: product, then task.
- Two entrance beats (seal, then card) plus a third only when the session-expired status is present. All three use `theme/motion.ts`.

**Non-Goals:**

- New copy, a password-reset flow, or a second accent color.
- Shell, home, tables, or GitHub Pages.
- Replacing the global CSS reduced-motion kill.

## Decisions

- Keep the incumbent 28px card radius and `apple-shadow`. The craft floor's 12–16px card note yields to the committed Apple token scale (`borderRadius.2xl`).
- Do not add glass or a left accent bar. Depth is the existing hairline-plus-soft shadow.
- Motion is opacity and `y: 4` only. Seal: `duration.slow` (240ms) and `ease.emphasized`. Card: `duration.base` (180ms), `ease.standard`, delay `stagger.step` (30ms). Session status: `duration.fast` (120ms), only when `expired=1`.
- Forgot-password stays a sentence, not a control. The product has no reset flow.

## Riesgos / Trade-offs

- [Playwright `toBeVisible` during the 240ms entrance] → Tests wait for the settled state; reduced-motion asserts `transform: none` immediately.
- [jsdom does not finish motion] → Unit tests assert structure and source constraints, not computed transforms. Playwright asserts the settled transform.
- [Card radius disagrees with a generic 16px craft default] → Documented as an incumbent override, not a new radius.

## Testing Strategy

| Scenario (spec) | Test level | Test class / file |
|-----------------|------------|-------------------|
| Credentials sit on one desk card | unit | `frontend/src/tests/unit/login-page.test.tsx` |
| Seal entrance does not rotate | unit (no rotate / no spring in source) and E2E | `login-page.test.tsx`; `testing/e2e/tests/TS-0001-login-authentication.spec.ts` |

- New unit tests: one case for the card, headings, and the absence of rotate/spring in `login/page.tsx`.
- New integration tests: none (no API change).
- Coverage impact: Vitest on an existing page; floors are not lowered.

TDD: the new unit test is run and observed failing before the page changes.

## Regression Strategy

- Existing tests affected: `login-page.test.tsx` (structure, lockout, session-expired, #1055). Assertions stay; the session-expired node remains `session-expired-message`.
- Full suite command: `bash workspace/sdlc/preflight.sh --full` (local gate, #2001).
- HTTP/Bruno API suite: included in that preflight; this change does not touch the API.
- Legacy paths at risk: none in `frontend-swing` or `jpa`.

## Playwright Strategy

- Specs to add/update under `testing/e2e/tests/`: `TS-0001-login-authentication.spec.ts`.
- Golden path covered: existing successful login, plus the card and headings visible at desktop.
- Edge / error paths covered: existing empty and bad credentials; new reduced-motion case that the seal's transform is `none`; a 320px check that the card fits without horizontal overflow.
- Viewports: 320px (overflow check) / 768px and 1024px (card and headings visible; the layout is a single centered column).
- Command: `cd testing/e2e && npx playwright test tests/TS-0001-login-authentication.spec.ts` when the stack is up; `preflight.sh --full` is the PR gate.

## Deployment Strategy

- Flyway migration required: no
- Deployment order / coupling: frontend image only, with the rest of the app
- Configuration or `.env` keys to add: none
- Feature flag: no
- Smoke test after deploy (Gate 5): open `/login`, confirm the card and a successful admin sign-in

## Rollback Strategy

- Revert safe: yes. No schema, no stored data, no cookie contract change.
- Database rollback: none needed
- Data written under the new behavior after revert: none
- Blast radius if rollback is delayed: visual only; sign-in still posts the same body

> Governed by [CONSTITUTION.md](../../../../CONSTITUTION.md) — §6 Quality Gates,
> §7 Testing Rules, §11 Release Rules.

## Context

`@media (prefers-reduced-motion: reduce)` in `globals.css` currently sets
`animation-duration` and `transition-duration` to `0.01ms !important` on every
element. Motion tokens already define `duration.instant` (0) for the JS path via
`MotionConfig reducedMotion="user"`. Issue #2073 asks to stop flattening state
transitions while still removing transform travel. Related: #1368, #2069.

## Goals / Non-Goals

**Goals:**
- Remove transform travel under reduced motion (no slide, scale, or rotate).
- Allow focus-ring and color/opacity state changes at `duration.fast`.
- Keep the login seal reporting `transform: none` under reduced motion.
- Keep MotionConfig `duration.instant` for the JS path.

**Non-Goals:**
- Redesigning motion tokens or product animations beyond the reduced-motion media query.
- Changing Wilson / portal / Context Lake.
- Enabling `[committer] required=true`.

## Decisions

**Choice for state transitions: `duration.fast` (not an instant cut).**
Focus rings and opacity/color status changes stay perceptible at 120ms. An
instant cut would still communicate "changed" but would keep the blink the issue
calls out. Recorded in CSS comment + Vitest.

**How transform travel is removed:**
1. CSS: exclude `transform` (and related travel properties) from transitions under
   reduced motion; keep `apple-button:active { transform: none }`.
2. Animations that are decorative may still use a near-zero `animation-duration`
   so keyframe travel does not run.
3. JS: `MotionConfig reducedMotion="user"` continues to drop transforms
   (`duration.instant` path unchanged) — login seal remains `transform: none`.

Alternative considered: instant cut (`transition-duration: 0` / `duration.instant`)
for all CSS transitions — rejected because AC allows `duration.fast` and the bug
is specifically the flattened focus/opacity blink.

## Riesgos / Trade-offs

[Risk] Allowing `duration.fast` globally could leave a residual transform
transition if an element still lists `transform` in `transition-property` →
Mitigation: reduced-motion block restricts transition properties to non-travel
(color, opacity, box-shadow, outline, …) and keeps press `transform: none`.
[Risk] Over-restricting `transition-property` breaks an intentional non-listed
property → Mitigation: allowlist matches the design-system "opacity + color /
shadow for state" rule; Vitest locks the allowlist text.
[Risk] Login seal regression → Mitigation: unit assertion on MotionConfig +
seal motion props / reduced-motion transform none contract.

## Testing Strategy

| Scenario (spec) | Test level | Test class / file |
|-----------------|------------|-------------------|
| No global 0.01ms transition kill; state uses duration.fast | unit | `motion-tokens.test.ts` |
| Transform travel removed under reduced motion | unit | `motion-tokens.test.ts` |
| Login seal transform none under reduced motion | unit | `login-page.test.tsx` |

- New unit tests: reduced-motion policy assertions in `motion-tokens.test.ts`;
  seal/MotionConfig regression in `login-page.test.tsx`
- New integration tests: n/a
- Coverage impact: frontend Vitest only; JaCoCo unchanged

## Regression Strategy

- Existing tests affected: `motion-tokens.test.ts` (add cases); `login-page.test.tsx` (add/extend seal case)
- Full suite command: `bash workspace/sdlc/preflight.sh --full`
- HTTP/Bruno API suite: n/a (no API surface)
- Legacy paths at risk: none

## Playwright Strategy

- Specs to add/update: none new; no interactive UI flow change beyond CSS media query
- Golden path covered: n/a — CSS/policy unit coverage is the proof
- Edge / error paths covered: n/a
- Viewports: n/a
- Command: n/a — no UI surface requiring Playwright for this change (unit locks the CSS contract)

## Deployment Strategy

- Flyway migration required: no
- Deployment order / coupling: frontend image only
- Configuration or `.env` keys to add: none
- Feature flag: no
- Smoke test after deploy (Gate 5): with OS reduced motion on, tab to a control and confirm the focus ring is not a zero-duration blink; login seal does not travel

## Rollback Strategy

- Revert safe: yes (CSS/docs/tests only)
- Database rollback: none needed
- Data written under the new behavior after revert: none
- Blast radius if rollback is delayed: a11y comfort only

## Migration Plan

None beyond the CSS media-query change, tests, and docs.

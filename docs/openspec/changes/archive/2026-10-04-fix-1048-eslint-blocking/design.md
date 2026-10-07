> Governed by [CONSTITUTION.md](../../../CONSTITUTION.md) — §6 Quality Gates,
> §7 Testing Rules, §11 Release Rules.

## Context

Issue #1048 (audit-2026-09, CU76): Frontend CI still treats ESLint as advisory.

Verified on workspace tip (2026-10-03):

| Location | Finding |
|----------|---------|
| `.github/workflows/frontend-ci.yml` ~44–49 | Comment cites #701; ESLint step has `continue-on-error: true` |
| Issue #701 | **CLOSED** — FlatCompat circular JSON crash fixed |
| `frontend/package.json` `lint` | `eslint src --max-warnings=0` |
| `scripts/preflight.sh` MAP | Documents “advisory in CI — #701; blocking here” while running blocking local eslint |
| `frontend/eslint.config.mjs` | Extends `eslint-config-next/core-web-vitals` + typescript; jsx-a11y comes via next; #1057 may tighten icon-button naming |

Issue text pairs with the icon-button `aria-label` work (**#1057**). Flip the CI
gate only after that Playwright-heavy change lands so a11y rule enablement and
known violation fixes are not fought on two concurrent branches.

Unrelated: `continue-on-error: true` on the Vitest test-reporter publish step
(~89) is out of scope.

## Goals / Non-Goals

**Goals:**

- ESLint failures fail `frontend-ci.yml` (no `continue-on-error` on that step).
- Existing lint / jsx-a11y violations fixed so `main` stays green after the flip.
- `preflight.sh` documents and keeps the same blocking semantics as CI.
- jsx-a11y rules enabled (core-web-vitals + any #1057 tightenings).

**Non-Goals:**

- Changing backend lint/format policy (Checkstyle, Spotless #705).
- Broader a11y product fixes beyond what lint reports (owned by #1057 / other issues).
- Flipping other advisory CI steps.
- Implementing before #1057 is on `main`.

## Decisions

1. **Fail closed by deleting `continue-on-error` on the ESLint step only**
   - Why: Matches AC; job already runs `npm run lint`. Setting
     `continue-on-error: false` is redundant — omit the key.
   - Alternative rejected: keep advisory forever — contradicts closed #701 and CU76.

2. **Serialize implement after #1057**
   - Why: #1057 enables/tightens jsx-a11y for icon buttons and fixes many a11y
     violations; both are Playwright-heavy / frontend-touching. Avoid dual
     conflict and “make blocking while tree is red” thrash.
   - Alternative rejected: land #1048 first with loose rules — leaves a11y gate soft.

3. **Parity: CI `npm run lint` ≡ preflight `eslint src --max-warnings=0`**
   - Why: Same package script / flags; update MAP comment so humans do not think
     CI is still advisory.
   - Alternative rejected: weaken preflight to match old CI — wrong direction.

4. **TDD via workflow/config asserts + red lint inventory**
   - Mechanical unit/script test: ESLint step in `frontend-ci.yml` MUST NOT set
     `continue-on-error: true`; MAP must not claim advisory #701.
   - Before flipping: capture `npm run lint` failures; fix them (or confirm #1057
     already did) so the first green CI run proves the gate.

5. **jsx-a11y via Next core-web-vitals (+ #1057 rule if present)**
   - Do not invent a second eslint stack. Confirm plugin/rules active; add
     explicit error-level rule only if #1057 left a gap vs AC.

## Riesgos / Trade-offs

- **[Risk] Large latent lint debt when gate flips** → Inventory after #1057 merge;
  fix all blocking findings in this PR before merge; do not re-enable
  `continue-on-error`.
- **[Risk] jsx-a11y false positives / noise** → Prefer Next recommended set;
  scoped overrides only with justification; no global disable.
- **[Risk] Dual-branch conflict with #1057 on eslint.config / pages** → Wait for
  #1057 merge (coordinator serialize).
- **[Risk] Preflight MAP drift again** → Unit assert on comment/MAP string or
  documented grep in config test.
- **[Trade-off] Blocking CI may temporarily slow merges** → Acceptable; quality
  gate is the point of CU76 / #1048.

## Testing Strategy

| Scenario (spec) | Test level | Test class / file |
|-----------------|------------|-------------------|
| ESLint step fails job on errors | unit / workflow | assert YAML: ESLint step has no `continue-on-error: true` |
| Clean tree passes blocking lint | command | `cd frontend && npm run lint` exit 0 |
| Preflight maps ESLint as blocking in CI | unit / script | assert `preflight.sh` MAP no longer says advisory #701 |
| Preflight/CI max-warnings parity | unit / doc | same `--max-warnings=0` / `npm run lint` |
| jsx-a11y enabled | unit / lint | eslint config includes next a11y / jsx-a11y rules |
| a11y violation fails lint | lint fixture | known-bad snippet or temporary fixture (delete after) |
| #701 comment removed | workflow grep | comment no longer says wait for #701 |

- New unit tests: prefer a small Node/shell/JUnit-free assert under an existing
  frontend or scripts test pattern used for workflow flags (mirror #1148 style).
- New integration tests: n/a (no backend).
- Coverage impact (JaCoCo): n/a — frontend/CI-only.

## Regression Strategy

- Existing tests affected: none expected for product logic; frontend lint may
  surface new errors on files touched by #1057 — fix in this change.
- Full suite command: `cd frontend && npm run lint && npm test`; PR heavy gate
  `bash scripts/check-heavy-ci.sh <pr>`; `bash scripts/preflight.sh` (or
  `--fix` if format needed).
- HTTP/Bruno API suite: n/a — no API change.
- Legacy paths: none.

## Playwright Strategy

- No new product E2E scenarios required for a CI-gate flip.
- Existing Playwright suite remains the UI regression gate on the PR (heavy CI).
- If this PR still fixes lint-driven UI/a11y source changes beyond #1057, run
  the affected Playwright specs; otherwise rely on full `playwright-e2e.yml`.
- Command: heavy gate includes Playwright; optional
  `cd frontend && npx playwright test` when source files change.

## Deployment Strategy

- Flyway migration required: no
- Deployment order / coupling: workflow + frontend lint fixes only; no schema
- Configuration or `.env` keys: none
- Feature flag: no
- Smoke test after deploy (Gate 5): open a PR or re-run Frontend CI on `main`
  and confirm the ESLint step fails closed on a deliberate local bad commit
  (optional) / green on clean tip

## Rollback Strategy

- Revert safe: yes — restore `continue-on-error: true` only as emergency
  (discouraged); prefer fix-forward on lint debt
- Database rollback: none needed
- Data written under the new behavior after revert: none
- Blast radius if rollback delayed: medium for merge velocity if latent lint
  noise; low for runtime product behavior

## Migration Plan

1. Wait for **#1057** merge to `main`.
2. Copy this draft into `openspec/changes/fix-1048-eslint-blocking/`.
3. Validate with `bash scripts/validate-sdlc-plan.sh fix-1048-eslint-blocking`.
4. Branch `cursor/fix-1048-eslint-blocking-69d3` → failing asserts → remove
   continue-on-error → fix lint → docs → PR `Closes #1048`.

## Open Questions

- Exact residual `npm run lint` error count after #1057 — measure at implement.
- Whether #1057 already enabled the icon-button jsx-a11y rule; if yes, #1048
  only verifies + fails closed CI; if no, enable here per AC.

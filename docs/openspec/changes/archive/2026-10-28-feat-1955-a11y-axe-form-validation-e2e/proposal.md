# Wave 7 — axe WCAG AA + form validation E2E

| Field | Value |
|-------|-------|
| GitHub Issue | #1955 |
| Also closes | #1251, #615 |
| Use Case | CU76; RNF-09 |
| Branch | `feat/1955_a11y-axe-form-validation-e2e` |
| Gate 1 status | passed |

## Objetivo

Epic #1941 Wave 7: automate WCAG 2.1 AA with `@axe-core/playwright` on login +
featured dashboard routes (allowlisted pre-existing findings), and prove empty
required fields on people/budgets/deeds/managements show FormField errors in E2E.

## What Changes

- Add `@axe-core/playwright`; TS-0155 axe suite + documented allowlist.
- TS-0156 form required-field E2E for four high-traffic create dialogs.
- Testing docs update.

## Impact

| Module | Touched |
|--------|---------|
| `testing` | yes (e2e) |
| `docs` | yes |
| `frontend` | no (unless axe forces a tiny fix) |

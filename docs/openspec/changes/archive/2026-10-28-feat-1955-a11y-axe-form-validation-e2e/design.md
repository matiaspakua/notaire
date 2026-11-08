# Design — #1955

## Approach

Parametrised Playwright tests call `AxeBuilder` with `wcag2a` + `wcag2aa` tags
after establishing an admin session. Violations whose `id`+`target` fingerprint
is in `axe-allowlist.json` (with issue link) are skipped; others fail.

Form validation E2E opens each create dialog, clicks save with empty required
fields, and asserts `role="alert"` (FormField error) stays visible and the
dialog remains open.

## Decisions

- Scan featured routes + `/login` (not every nested admin page) for runtime cost.
- Allowlist is ratchet-only: entries require an issue number in the JSON.
- No frontend product changes unless a new serious/critical axe finding is trivial.

## Out of scope

- Full dialog open-state axe matrix (follow-up from first-scan findings)
- Fixing every allowlisted finding in this PR

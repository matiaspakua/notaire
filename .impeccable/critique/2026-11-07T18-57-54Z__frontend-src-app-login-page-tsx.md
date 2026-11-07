---
target: frontend/src/app/login/page.tsx
total_score: 26
max_score: 40
na_heuristics: 
p0_count: 0
p1_count: 3
target_identity: "file:/Users/matiasmiguez/workspace/notaire-wt-2069/frontend/src/app/login/page.tsx"
target_fingerprint: "sha256:32baf12c35a399db78fcef7950604bcf26e33cc4b88c322c6ea478a45922121f"
target_path: /Users/matiasmiguez/workspace/notaire-wt-2069/frontend/src/app/login/page.tsx
timestamp: 2026-11-07T18-57-54Z
slug: frontend-src-app-login-page-tsx
---
⚠️ DEGRADED: single-context (this session is already a subagent; the parent forbids nested subagents, so Assessment A and Assessment B were not isolated)

Method note: design review was completed before the detector output was folded in. The detector was then run on the login page, sidebar, data table, empty state, dashboard home, and form patterns.

## Design Health Score

| # | Heuristic | Score | Key Issue |
|---|-----------|-------|-----------|
| 1 | Visibility of System Status | 3 | Skeletons, toasts, and session-expired exist. Sign-in loading is still a bare ellipsis. |
| 2 | Match System / Real World | 3 | Spanish notarial terms (gestión, testimonio, folio) match the office. |
| 3 | User Control and Freedom | 3 | Collapsible nav, breadcrumbs, logout. Many edits are still dialogs. |
| 4 | Consistency and Standards | 2 | Login seal used a spring rotation; table rows use a 300ms Material ease instead of theme/motion.ts. |
| 5 | Error Prevention | 3 | Required fields, lockout copy, and confirm dialogs are in place. |
| 6 | Recognition Rather Than Recall | 3 | Sidebar labels are visible. The home grid repeats them without priority. |
| 7 | Flexibility and Efficiency | 2 | No quicker path than the full sidebar for a clerk who already knows the destination. |
| 8 | Aesthetic and Minimalist Design | 2 | Login stacked three peer lines on the bare page. Home modules share one blue well and a decorative icon scale. |
| 9 | Error Recovery | 3 | Dashboard and tables offer retry. Lockout names the block. |
| 10 | Help and Documentation | 2 | Empty tables are a title only. Forgot-password is a sentence, which is honest, and there is no next step inside an empty list. |
| **Total** | | **26/40** | **Acceptable (65%)** |

## Design Specificity Verdict

**LLM assessment.** The authenticated app is authored for a notary desk: San Francisco Blue pinned at #0071E3, SF Pro, 28px cards, Spanish case language, and a protocol sidebar. It is not a generic purple SaaS shell. The failure is inside that system. Login, the first Operate surface, did not use the desk card or the motion tokens. The home module grid is a second, flatter copy of the sidebar. Tables are recognizable and slightly anonymous because every row enters with the same off-token fade.

**Deterministic scan.** `impeccable detect --json` on login, AppSidebar, DataTable, EmptyState, dashboard page, and form-patterns returned `[]` (exit 0). It did not catch the DataTable duration `0.3` or the login `rotate: -8`. Treat that as a false negative, not a clean bill.

**Visual overlays.** No overlay was injected. The live-server path was skipped because this pass had no isolated browser assessment and the product app is not a static file target.

## Overall Impression

The token system is the real design. The highest-leverage break was the login page: no card, three competing lines, and a decorative spring. That is the tip. What remains is motion drift in the table and empty states that do not teach the next action.

## What's Working

- FormContainer / FormField / FormActions on login, with real labels and a session-expired status.
- MotionConfig `reducedMotion="user"` plus a shared motion.ts contract for routes and dialogs.
- The sidebar's current-item pill, focus rings, and mobile dialog sheet.

## Priority Issues

- **[P1] Login was not the desk.** Three lines, no surface, spring rotation. Fix: one card, one h1, token-timed rise. Suggested command: $impeccable layout
- **[P1] Table rows ignore motion tokens.** 300ms, Material ease, stagger up to 300ms. Suggested command: $impeccable animate
- **[P1] Empty tables do not name the next action.** Title only. Suggested command: $impeccable onboard
- **[P1] Home modules are one accent repeated.** Decorative scale-110. Suggested command: $impeccable distill
- **[P2] Reduced motion is a global 0.01ms kill.** Suggested command: $impeccable animate
- **[P2] Mobile nav group targets are 32px.** Suggested command: $impeccable adapt

## Persona Red Flags

- **Notary clerk (mid-morning, one case open).** The home grid does not say what is urgent. Expirations exist as a card, then every module looks equally blue.
- **Administrator configuring catalogs.** Empty catalogs say there is no data and do not point at the create button already on the page.
- **Keyboard user with reduced motion.** Route motion respects the setting. The CSS kill also flattens every transition, including focus, to a blink.

## Minor observations

- The dashboard date chip sets ink with an inline style instead of a semantic class.
- Sidebar group labels are 11px uppercase. They scan, and they are the only uppercase in the chrome.

Questions skipped: this run is a subagent with no channel back to the owner. The ranked issues were filed instead of waiting for a choice.

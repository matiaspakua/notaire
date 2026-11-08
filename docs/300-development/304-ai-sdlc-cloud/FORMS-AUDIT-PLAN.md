# Forms audit plan — code inspection only

Phase 1 inventory for product forms. **The rendered audit did not run.**
No browser, no contrast measurement, no keyboard pass, no touch gesture, and
no Impeccable health score. Keyword hits are not a pass.

Impeccable context was loaded for `frontend/src/theme/form-patterns.tsx`
(Operate mode, `PRODUCT.md` / `DESIGN.md`). Provenance, from the repo and
GitHub on 2026-11-08:

| Fact | Value |
|------|--------|
| Source | `pbakaus/impeccable` (`skills-lock.json`, `sourceType: github`) |
| Skill metadata | 4.8.1 (`.claude/skills/impeccable/SKILL.md`; upstream default branch matched) |
| Launcher | `.claude/skills/impeccable/scripts/VERSION` = `0.1.19` |
| License | Apache-2.0 (`gh api repos/pbakaus/impeccable`) |
| Install | `.claude/skills/impeccable` is a symlink to `.agents/skills/impeccable` |

`.impeccable/design.json` is older than `DESIGN.md` (Impeccable `CONTEXT_STALE`,
severity mention). This plan does not refresh it. Run `$impeccable document`
only when someone asks.

Detector: not run. A previous critique
(`.impeccable/critique/2026-11-07T18-57-54Z__frontend-src-app-login-page-tsx.md`)
already recorded that `impeccable detect --json` returned `[]` on login and
missed real motion drift. Do not treat an empty detector as a clean bill.

## Checklist for the rendered pass

Each issue below uses Impeccable `audit` (technical) on a running page, then
`harden` only for the failures that issue owns. Score a dimension only after
the rendered check. Desktop and a 320px viewport. Record the engine.

| Check | What "done" means |
|-------|-------------------|
| Labels | Every control has a visible label, not only a placeholder |
| Required | Required state is in text and in the accessible name |
| Errors | Error text uses the error token, is tied to the field, and says the recovery |
| Keyboard / focus | Tab order follows the visual order; focus ring visible; no trap |
| WCAG | Contrast measured on the rendered pixels (AA). Source tokens are not a measurement |
| Responsive / touch | 320px does not clip the primary action. Large buttons stay at least 44px; WCAG 2.2 AA floor is 24px |
| Loading | Submit disables or shows a busy state and does not double-post |
| Empty | A list or picker with no rows names the gap and one next action when one exists |
| Disabled | Disabled fill is not the readable-text color |
| Consistency | `FormContainer` → `FormSection` → `FormField` → `FormActions`, tokens only |

## Shared contract

- `frontend/src/theme/form-patterns.tsx`
- `frontend/src/theme/form-field-context.tsx`
- `frontend/src/components/ui/input.tsx`
- `frontend/src/components/ui/select.tsx`

## Surfaces found in source (42 files)

Presence of `FormContainer`, `<form`, `FormField`, or `CheckboxField` under
`frontend/src`, tests excluded. The signal column is a case-insensitive
substring (`disabled`, `loading`/`isLoading`, `aria-invalid`, `error`,
`empty`, `required`). It is not behavior.

| Surface | Primitives in source | Substrings |
|---------|----------------------|------------|
| `app/login/page.tsx` | form, FormContainer, FormField, FormActions | disabled, loading, error, required |
| `app/dashboard/people/page.tsx` | FormContainer, FormField, CheckboxField, FormActions | disabled, loading, aria-invalid, error, empty, required |
| `app/dashboard/managements/page.tsx` | FormContainer, FormField, FormActions | disabled, loading, error, empty, required |
| `app/dashboard/managements/ManagementSummaryDialog.tsx` | FormContainer | loading, empty |
| `app/dashboard/budgets/page.tsx` | FormContainer, FormField, FormActions | disabled, loading, error, empty, required |
| `app/dashboard/payments/page.tsx` | FormContainer, FormField, FormActions | disabled, loading, aria-invalid, error, empty, required |
| `app/dashboard/deeds/page.tsx` | FormContainer, FormField, FormActions | disabled, loading, error, empty, required |
| `app/dashboard/copies/page.tsx` | FormContainer, FormField, FormActions | disabled, loading, aria-invalid, error, empty, required |
| `app/dashboard/documents/page.tsx` | FormContainer, FormField, CheckboxField, FormActions | disabled, loading, error, empty, required |
| `app/dashboard/external-entity-documents/page.tsx` | FormContainer, FormField, CheckboxField, FormActions | disabled, loading, error, empty |
| `app/dashboard/items/page.tsx` | FormContainer, FormField, FormActions | disabled, loading, error, empty, required |
| `app/dashboard/properties/page.tsx` | FormContainer, FormField, FormActions | disabled, loading, error, empty, required |
| `app/dashboard/registration-drafts/page.tsx` | FormContainer, FormField, FormActions | disabled, loading, error, required |
| `app/dashboard/reports/page.tsx` | FormField | loading, error, required |
| `app/dashboard/required-documents/page.tsx` | FormContainer, FormField | loading, error, empty, required |
| `app/dashboard/substitutions/page.tsx` | FormContainer, FormField, FormActions | disabled, loading, error, empty |
| `app/dashboard/upcoming-expirations/page.tsx` | FormField | loading, error, empty |
| `app/dashboard/testimonies/page.tsx` | FormContainer, FormField, CheckboxField, FormActions | disabled, loading, error, empty, required |
| `app/dashboard/testimonies/RegisterTestimonyDialog.tsx` | FormContainer, FormField, CheckboxField, FormActions | disabled, aria-invalid, error, required |
| `app/dashboard/testimonies/TestimonyMovementsDialog.tsx` | FormContainer | empty |
| `app/dashboard/testimony-movements/page.tsx` | FormContainer, FormField, FormActions | disabled, loading, error, empty, required |
| `app/dashboard/testimony-movements/ReenterDialog.tsx` | FormContainer, FormField, CheckboxField, FormActions | disabled, required |
| `app/dashboard/protocol/page.tsx` | FormContainer, FormField, FormActions | disabled, loading, error, empty, required |
| `app/dashboard/protocol/auxiliary/page.tsx` | FormContainer, FormField, FormActions | disabled, loading, error, empty, required |
| `app/dashboard/protocol/notebooks/page.tsx` | FormContainer, FormField, CheckboxField, FormActions | disabled, loading, error, empty |
| `app/dashboard/administration/concepts/page.tsx` | FormContainer, FormField, FormActions | disabled, loading, aria-invalid, error, empty, required |
| `app/dashboard/administration/documents/page.tsx` | FormContainer, FormField, CheckboxField, FormActions | disabled, loading, error, empty, required |
| `app/dashboard/administration/folios/page.tsx` | FormContainer, FormField, CheckboxField, FormActions | disabled, loading, error, empty, required |
| `app/dashboard/administration/identification-types/page.tsx` | FormContainer, FormField, FormActions | disabled, loading, error, empty, required |
| `app/dashboard/administration/management-statuses/page.tsx` | FormContainer, FormField, FormActions | disabled, loading, error, empty, required |
| `app/dashboard/administration/procedures/page.tsx` | FormContainer, FormField, CheckboxField, FormActions | disabled, loading, error, empty, required |
| `app/dashboard/administration/procedures/RequiredDocumentsDialog.tsx` | FormContainer, FormField | disabled, loading, error, empty, required |
| `app/dashboard/administration/roles/page.tsx` | FormContainer, FormField, FormActions | disabled, loading, aria-invalid, error, empty, required |
| `app/dashboard/administration/templates/page.tsx` | FormContainer, FormField, FormActions | disabled, loading, error, empty, required |
| `app/dashboard/administration/users/page.tsx` | FormContainer, FormField, FormActions | disabled, loading, aria-invalid, error, empty, required |
| `app/dashboard/administration/workflows/page.tsx` | FormContainer, FormField, FormActions | disabled, loading, error, empty, required |
| `app/dashboard/administration/workflows/[id]/page.tsx` | FormContainer, FormField, FormActions | disabled, error, empty, required |
| `components/workflow/WorkflowHero.tsx` | form | loading, error, empty |
| `components/ui/input.tsx` | uses form-field aria | disabled |
| `components/ui/select.tsx` | uses form-field aria | disabled |
| `theme/form-field-context.tsx` | aria helper | aria-invalid, error, required |
| `theme/form-patterns.tsx` | shared primitives | disabled, error, required |

Login already has a 2026-11-07 critique and the desk fix (#2069). Do not
re-open that layout in the next form issue. #2070–#2074 stay the motion,
empty-table, home-grid, reduced-motion, and 32px nav findings. They are not
this form pass.

## Next iterations

1. [#2087](https://github.com/matiaspakua/notaire/issues/2087) — rendered audit
   of the shared controls plus `people/page.tsx`. Blocked by #2086.
2. [#2088](https://github.com/matiaspakua/notaire/issues/2088) — administration
   catalog pages in the table above. Blocked by #2087.
3. Case modules (managements, deeds, budgets, payments, testimonies, protocol)
   get their own issue only after #2088 shows a repeated gap. Do not open that
   issue from this plan.

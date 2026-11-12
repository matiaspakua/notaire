# Forms audit plan — code inspection + #2087/#2088 rendered slices

Phase 1 inventory for product forms remains a **code inspection** (source-only
keyword hits are not a pass for other surfaces). **#2087 rendered the people
create/edit dialog and shared FormField controls** (Playwright Chromium,
desktop 1280 and 320px): labels, required `*` + `aria-required`, linked
`role="alert"` errors, tab order, primary `h-11` (~44px), empty-list next
action, and CheckboxField raised to the WCAG 2.2 AA 24px floor. Evidence:
`docs/openspec/changes/archive/*-fix-2087-people-form-rendered-audit/` (after
archive) and the PR for #2087.

**#2088 rendered administration catalog forms** (concepts, documents, folios,
identification-types, management-statuses, procedures + RequiredDocumentsDialog,
roles, templates, users, workflows + workflows/[id]) at 1280 and 320px with the
same checklist. Toast-only client validation moved to FormField `error` /
`aria-invalid`; raw checkboxes on roles/workflows use CheckboxField; primary
saves expose `aria-busy`. Evidence: `docs/openspec/changes/archive/*-fix-2088-admin-catalog-forms-audit/`
(after archive), `TS-0158`, and the PR for #2088. Case modules
(managements/deeds/protocol/…) remain for later domain PRs.

**#2259 module 1, `managements` page and create dialog** (Chromium, 1280 and 320px, axe wcag2a/2aa/21aa/22aa, 2026-11-12): 0 axe violations, no horizontal overflow, every control labelled, empty submit shows 5 required alerts. One failure: only 3 of the 5 alerts were tied to an `aria-invalid` control, because the shared `SearchCombobox` (Presupuesto, Escribano) ignored the FormField state. Fixed by reading `useFormFieldAria`; after the fix 5 of 5 on both viewports. Evidence: `docs/openspec/changes/archive/*-fix-2259-management-form-combobox-aria/`, `TS-0159`. The 34x20px breadcrumb link is an inline target and stays under the WCAG 2.5.8 spacing exception.

**#2259 module 2, `payments` page and create dialog** (same method, 2026-11-12): 0 axe violations, no horizontal overflow at 1280 and 320px, all controls labelled, empty submit shows 3 required alerts each tied to an `aria-invalid` control (Presupuesto through the `SearchCombobox` fix of module 1), primary 44px, delete behind `ConfirmDialog`, submit disabled while pending. One gap: the receipt row action was named by `title` alone (edit and delete had `aria-label`); it now has one. Evidence: `docs/openspec/changes/archive/*-fix-2259-payments-receipt-aria-label/`. Row action target sizes on a populated list were not measured (no seeded payment).

**#2259 modules 3 to 9, batch pass** (same method; `deeds`, `budgets`, `copies`, `items`, `people`, plus the earlier `managements` and `payments`; 2026-11-12): all pass axe on the list and the create dialog at 1280 and 320px, none scrolls sideways, every required error on `deeds`, `budgets`, `copies` and `people` is tied to an `aria-invalid` control, primaries are 44px, and signing or deleting a deed goes through `ConfirmDialog` with a loading guard. Findings: (1) the filled primary button lost AA contrast while hovered (`hover:bg-primary/90` gave 4.01:1, white text), fixed for the default and apple variants by hovering to the existing `primary-text` token (about 6.4:1); (2) `items`: the conditional required reason was validated by a toast only; it is now a `FormField` error with `aria-invalid` and `aria-describedby`, cleared on typing (`fix-2259-items-reason-inline-error`); (3) `documents`, `properties` and `substitutions` dialogs have a differently labelled submit button the batch script did not reach were measured separately (see below).

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
   of the shared controls plus `people/page.tsx`. **Done** (Playwright Chromium
   desktop + 320px; CheckboxField AA floor; primary `h-11` locked).
2. [#2088](https://github.com/matiaspakua/notaire/issues/2088) — administration
   catalog pages in the table above. Blocked by #2087.
3. Case modules (managements, deeds, budgets, payments, testimonies, protocol)
   get their own issue only after #2088 shows a repeated gap. Do not open that
   issue from this plan.

**#2259 `substitutions`, `documents`, `properties`** (same method, 2026-11-12): all three pass axe and have no sideways scroll at 1280 and 320px. `substitutions`: the API requires `dateStart` and `dateEnd` (400 `must not be null`, verified) but the dialog did not mark them required and an empty save produced no field error; they are now required fields with `FormField` errors, cleared on input, and nothing is sent until both are set (`fix-2259-substitutions-required-dates`). `documents` and `properties` use another pattern: the save button is **disabled until the required fields are valid** (properties: `cadastralDesignation`, CU69 #655), so an empty save shows no error and the disabled button does not say what is missing. That is recorded as an open design question (disabled-until-valid versus validate-on-submit) and was not changed.

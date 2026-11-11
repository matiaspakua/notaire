# Rendered audit of administration catalog forms

> Governed by [CONSTITUTION.md](../../../../CONSTITUTION.md). This proposal documents
> **only this change**; permanent documentation remains the single source of truth.

| Field | Value |
|-------|-------|
| GitHub Issue | #2088 |
| Use Case | CU76 — Quality Assurance and Testing Infrastructure; RNF-07 |
| Branch | `fix/2088_admin-catalog-forms-audit` |
| Gate 1 status | passed |

## Objetivo

Phase 1 (#2086) only keyword-scanned administration catalog forms. After #2087 locked the shared FormField contract on people, these Operate catalogs need the same rendered pass (desktop + 320px) so clerks can recover from labels, errors, keyboard, disabled/loading/empty, and touch gaps.

## What Changes

Run Impeccable context/detect and a rendered Playwright Chromium audit on the listed administration surfaces; harden shared FormContainer/FormField/FormActions/CheckboxField drift once; add failing-then-green unit and Playwright locks. One domain-sized PR. Do not touch people, managements, deeds, or protocol pages.

## Reglas de negocio

| Rule | Source | New / Changed / Made explicit |
|------|--------|-------------------------------|
| Administration catalog create/edit forms SHALL expose visible labels, required indicators, linked error text, keyboard focus order, honest disabled/loading/empty states, and primary actions that meet WCAG 2.2 AA 24px (design system prefers 44px / `h-11`). Shared contract drift SHALL be fixed in shared components, not copied per page. | CU76 / RNF-07 / #2088 | New |

## Capabilities

### New Capabilities

- `ui-admin-catalog-forms-a11y`: Rendered administration catalog dialogs and shared FormField controls meet the form-pattern accessibility contract (labels, required, errors, focus, states, touch targets) at desktop and 320px.

### Modified Capabilities

## Impact Analysis

### Módulos afectados

| Module | Touched | What changes |
|--------|---------|--------------|
| `backend-api` | no | — |
| `frontend` | yes | administration catalog pages, shared form-patterns if needed, unit + Playwright locks |
| `frontend-swing` | no | — |
| `infra` / observability | no | — |
| CI/CD (`.github/workflows`) | no | — |
| `workspace` (harness) | yes | OpenSpec change + FORMS-AUDIT-PLAN rendered-pass note |

### Surface area

- Entities: none
- Endpoints: none
- Database (Flyway `V{n}`): none
- Configuration / `.env`: none
- Dependencies: none

### Write set

- `frontend/src/app/dashboard/administration/concepts/**`
- `frontend/src/app/dashboard/administration/documents/**`
- `frontend/src/app/dashboard/administration/folios/**`
- `frontend/src/app/dashboard/administration/identification-types/**`
- `frontend/src/app/dashboard/administration/management-statuses/**`
- `frontend/src/app/dashboard/administration/procedures/**`
- `frontend/src/app/dashboard/administration/roles/**`
- `frontend/src/app/dashboard/administration/templates/**`
- `frontend/src/app/dashboard/administration/users/**`
- `frontend/src/app/dashboard/administration/workflows/**`
- `frontend/src/theme/form-patterns.tsx`
- `frontend/src/theme/form-field-context.tsx`
- `frontend/src/components/ui/input.tsx`
- `frontend/src/components/ui/select.tsx`
- `frontend/src/components/ui/button.tsx`
- `frontend/src/tests/unit/**`
- `testing/e2e/tests/**`
- `docs/openspec/**`
- `docs/300-development/304-ai-sdlc-cloud/FORMS-AUDIT-PLAN.md`
- `CHANGELOG.md`
- `.impeccable/**`

### Architecture review

Follows the existing architecture and design-system FormContainer → FormSection → FormField → FormActions contract; no ADR.

## Documentation Impact

| Permanent document | What must change |
|--------------------|------------------|
| `docs/300-development/304-ai-sdlc-cloud/FORMS-AUDIT-PLAN.md` | Record that #2088 rendered audit ran (desktop + 320px) for administration catalogs |
| `CHANGELOG.md` | [Unreleased] admin catalog rendered a11y audit locks (omit if another open PR holds the fence) |

## Out of Scope

People form (#2087, already merged); managements, deeds, budgets, payments, testimonies, protocol and other case modules; GitHub Pages restyle; #2070–#2074 motion/empty/home/nav findings.

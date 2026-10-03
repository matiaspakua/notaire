# frontend-i18n-page-coverage Specification

## Purpose

Close the frontend i18n coverage gap for remaining dashboard pages and login
leftover strings so English locale switching (TS-0040) does not leave Spanish
hardcoded copy on those surfaces. Source: #1059; CU76; ADR-015.

## Requirements

### Requirement: Gap page namespaces exist in both catalogs

The message catalogs `frontend/messages/es.json` and `frontend/messages/en.json`
SHALL contain page namespaces (or nested keys under an existing parent) covering
workflows list/editor, roles, suplencias, reportes, items (canonical dashboard
page), and any new auditoria/login keys required by this change. Both catalogs
MUST expose identical key structures.

#### Scenario: Gap page namespaces exist in both catalogs

- **WHEN** the unit i18n integrity suite runs
- **THEN** required namespaces/keys for workflows, roles, suplencias, reportes,
  items, and auditoria leftovers are present in both `es` and `en` catalogs

#### Scenario: Catalog key structures stay identical

- **WHEN** a key is added to one locale catalog
- **THEN** the matching key MUST exist in the other locale, or the unit test fails

### Requirement: Login leftover strings are catalog-driven

The login page SHALL resolve connection, lockout, validation, welcome, forgot-
password, and secure-footer user-visible strings through the `login` namespace
(or shared common keys where appropriate). Hardcoded Spanish literals for those
concerns MUST NOT remain in `login/page.tsx`.

#### Scenario: Login leftover keys exist in both catalogs

- **WHEN** the unit i18n integrity suite runs
- **THEN** login keys for connection, lockout, validation, welcome, forgot, and
  footer are present in both catalogs with non-empty values

#### Scenario: Login page uses translated leftovers

- **WHEN** a user triggers validation/connection/lockout/welcome/forgot/footer UI
- **THEN** the visible copy comes from next-intl translations for the active locale

### Requirement: Gap UI pages call useTranslations for user-visible copy

The gap UI pages (workflows list, workflows editor, suplencias, reportes, roles,
dashboard/items) SHALL call `useTranslations` for page-specific copy and reuse
`common` for shared actions/labels where an existing key fits. Redirect-only
stubs (`/`, `/auditoria`, `administracion/items`, `administracion/auditoria`)
are exempt because they have no UI strings.

#### Scenario: Workflows list and editor use translations

- **WHEN** a user opens `/dashboard/administracion/workflows` or the editor
- **THEN** titles, toasts, form labels, and empty states resolve via next-intl

#### Scenario: Suplencias, reportes, roles, and items use translations

- **WHEN** a user opens those pages
- **THEN** titles, toasts, form labels, columns, and empty states resolve via
  next-intl (roles completes remaining hardcoded strings beyond common edit/delete)

#### Scenario: Auditoria all-modules filter is translated

- **WHEN** a user opens the canonical auditoria page filter
- **THEN** the all-modules option label is translated (not a Spanish literal)

### Requirement: Missing required keys fail tests

The unit suite `frontend/src/tests/unit/i18n.test.ts` SHALL fail when any
required namespace or key introduced by this change is missing from either
catalog.

#### Scenario: Missing required namespace fails the suite

- **WHEN** a required gap-page or login leftover key is removed from a catalog
- **THEN** `i18n.test.ts` fails

#### Scenario: Optional EN title coverage on former gap pages

- **WHEN** TS-0040 (or a focused extension) runs against a wired gap page with
  locale `en`
- **THEN** at least one former gap page title is asserted in English

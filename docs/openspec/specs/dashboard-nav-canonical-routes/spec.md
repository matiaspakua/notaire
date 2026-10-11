# dashboard-nav-canonical-routes Specification

## Purpose

Make substitutions and reports discoverable from primary dashboard navigation,
collapse duplicate administration pages for items and audit into one canonical
route each, and prove end-to-end tests reach those screens via navigation.
Source: #1058; CU22, CU59, CU24, CU25, CU50, CU23. Sidebar label keys stay
`suplencias`, `reportes`, `auditoria`, and `administracion`.

## Requirements

### Requirement: Substitutions and reports appear in primary navigation

The dashboard sidebar MUST expose navigation entries for substitutions
(`/dashboard/substitutions`, label key `suplencias`, test id `nav-suplencias`)
and reports (`/dashboard/reports`, label key `reportes`, test id
`nav-reportes`) with localized labels. Visibility MUST be role-appropriate:
entries are shown to authenticated users who are allowed to use those modules.
The administration entry (label key `administracion`) remains admin-gated.

#### Scenario: Sidebar links to substitutions

- **WHEN** an authenticated user opens the dashboard sidebar
- **THEN** the `nav-suplencias` control is present and navigates to
  `/dashboard/substitutions` without using a hard-coded test-only deep link

#### Scenario: Sidebar links to reports

- **WHEN** an authenticated user opens the dashboard sidebar
- **THEN** the `nav-reportes` control is present and navigates to
  `/dashboard/reports`

### Requirement: Items and audit have one canonical route each

The application MUST NOT maintain separate full page implementations under
`/dashboard/administration/items` and `/dashboard/administration/audit` that
duplicate `/dashboard/items` and `/dashboard/audit`. Old admin paths MUST
redirect (or otherwise resolve) to the canonical routes. The audit label key
stays `auditoria`.

#### Scenario: Canonical items route only

- **WHEN** a user navigates to items from the product UI after this change
- **THEN** they land on `/dashboard/items` (not a second full duplicate page
  under administration), and `/dashboard/administration/items` redirects or is
  removed

#### Scenario: Canonical audit route only

- **WHEN** a user navigates to audit from the product UI after this change
- **THEN** they land on `/dashboard/audit`, and
  `/dashboard/administration/audit` redirects or is removed

### Requirement: End-to-end tests reach substitutions and reports via navigation

Playwright coverage for substitutions and reports discovery MUST use the
navigation UI (sidebar or equivalent primary nav) rather than only
`page.goto` deep links for the acceptance path described in #1058.

#### Scenario: End-to-end opens substitutions through nav

- **WHEN** the updated Playwright suite runs the substitutions discovery path
- **THEN** it activates `nav-suplencias` and asserts the canonical URL and
  content load

#### Scenario: End-to-end opens reports through nav

- **WHEN** the updated Playwright suite runs the reports discovery path
- **THEN** it activates `nav-reportes` and asserts the canonical URL and
  content load

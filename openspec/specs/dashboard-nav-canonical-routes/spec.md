# dashboard-nav-canonical-routes Specification

## Purpose
Make Suplencias and Reportes discoverable from primary dashboard navigation,
collapse duplicate administración pages for Items and Auditoría into one
canonical route each, and prove E2E reaches those screens via navigation UI.
Source: #1058; CU22, CU59, CU24, CU25, CU50, CU23.
## Requirements
### Requirement: Suplencias and Reportes appear in primary navigation

The dashboard sidebar MUST expose navigation entries for Suplencias
(`/dashboard/suplencias`) and Reportes (`/dashboard/reportes`) with localized
labels. Visibility MUST be role-appropriate: entries are shown to authenticated
users who are allowed to use those modules; Administración remains admin-gated.

#### Scenario: Sidebar links to Suplencias

- **WHEN** an authenticated user opens the dashboard sidebar
- **THEN** a Suplencias navigation control is present and navigates to
  `/dashboard/suplencias` without using a hard-coded test-only deep link

#### Scenario: Sidebar links to Reportes

- **WHEN** an authenticated user opens the dashboard sidebar
- **THEN** a Reportes navigation control is present and navigates to
  `/dashboard/reportes`

### Requirement: Items and Auditoría have one canonical route each

The application MUST NOT maintain separate full page implementations under
`/dashboard/administracion/items` and `/dashboard/administracion/auditoria` that
duplicate `/dashboard/items` and `/dashboard/auditoria`. Old admin paths MUST
redirect (or otherwise resolve) to the canonical routes.

#### Scenario: Canonical Items route only

- **WHEN** a user navigates to Items from the product UI after this change
- **THEN** they land on `/dashboard/items` (not a second full duplicate page
  under administración), and `/dashboard/administracion/items` redirects or is
  removed

#### Scenario: Canonical Auditoría route only

- **WHEN** a user navigates to Auditoría from the product UI after this change
- **THEN** they land on `/dashboard/auditoria`, and
  `/dashboard/administracion/auditoria` redirects or is removed

### Requirement: E2E reaches Suplencias and Reportes via navigation

Playwright coverage for Suplencias and Reportes discovery MUST use the
navigation UI (sidebar or equivalent primary nav) rather than only
`page.goto` deep links for the acceptance path described in #1058.

#### Scenario: E2E opens Suplencias through nav

- **WHEN** the updated Playwright suite runs the Suplencias discovery path
- **THEN** it clicks/activates the Suplencias nav control and asserts the
  canonical URL/content loads

#### Scenario: E2E opens Reportes through nav

- **WHEN** the updated Playwright suite runs the Reportes discovery path
- **THEN** it clicks/activates the Reportes nav control and asserts the
  canonical URL/content loads


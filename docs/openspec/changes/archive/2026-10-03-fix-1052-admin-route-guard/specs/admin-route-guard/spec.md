## Purpose

Define how the Notaire web client blocks non-admin authenticated users from
opening administración screens via direct URL, using both the edge layer and a
layout guard, and informs the user when access is denied.

## ADDED Requirements

### Requirement: Admin routes require an admin-capable role

Authenticated access to `/dashboard/administracion` and its sub-paths SHALL be
allowed only for admin-capable user types (`ADMIN`, `ADMINISTRADOR`, `ESCRIBANO`).
Source: CU78 – Security and Compliance (step 3 / alt 3.1); CU20/CU21 admin actors.

#### Scenario: Non-admin edge deny for administración path

- **WHEN** an authenticated session presents a non-admin role signal and requests `/dashboard/administracion` or any sub-path
- **THEN** the edge layer redirects to `/dashboard?forbidden=1` and does not serve the admin page

#### Scenario: Admin edge allow for administración path

- **WHEN** an authenticated session presents an admin-capable role signal and requests `/dashboard/administracion` or any sub-path
- **THEN** the edge layer allows the request to proceed

#### Scenario: Non-admin layout redirects with forbidden flag

- **WHEN** a non-admin authenticated user reaches the administración layout (for example missing or stale edge role signal)
- **THEN** the client redirects to `/dashboard?forbidden=1`

### Requirement: Access denial is visible to the user

When redirected for missing admin privileges, the user MUST see a clear
access-denied message on the dashboard.

#### Scenario: Dashboard shows access-denied message

- **WHEN** the dashboard is opened with `forbidden=1` in the query string
- **THEN** the user sees a clear message that they lack permission for that section

#### Scenario: Non-admin cannot open admin routes end-to-end

- **WHEN** a non-admin user (for example `EMPLEADO`) logs in and navigates to `/dashboard/administracion/usuarios`
- **THEN** the user lands on the dashboard with the forbidden signal and message, and does not see the admin usuarios UI

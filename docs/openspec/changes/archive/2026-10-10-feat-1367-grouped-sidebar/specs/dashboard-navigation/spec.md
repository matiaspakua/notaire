# dashboard-navigation — delta

## Purpose

The dashboard sidebar groups its links into task-based sections shared with the dashboard module grid.

## MODIFIED Requirements

### Requirement: Sidebar sections

The dashboard sidebar SHALL show Inicio followed by the six approved sections, each with a heading that expands or collapses it; the section containing the current route SHALL be expanded; collapsed sections SHALL persist per browser; the dashboard module grid SHALL be derived from the same configuration; admin-only items SHALL match the admin-guarded routes.

#### Scenario: Admin sees six sections

- **WHEN** an admin opens /dashboard/copias
- **THEN** the six section headings show in order and Escrituras y protocolo is expanded with Copias current

#### Scenario: Collapse persists

- **WHEN** the user collapses Clientes and reloads
- **THEN** Clientes stays collapsed until the user visits /dashboard/personas, which opens it

#### Scenario: Shared config

- **WHEN** the dashboard home renders
- **THEN** the module cards are the featured nav items in sidebar order

#### Scenario: Non-admin

- **WHEN** an EMPLEADO signs in
- **THEN** five sections show and neither Auditoría nor Administración is linked

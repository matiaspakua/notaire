# ui-accessibility — delta

## Purpose

Dashboard controls and dialogs expose accessible names in the active locale.

## MODIFIED Requirements

### Requirement: Filter selects are named

Each filter `SelectTrigger` or `select` on dashboard pages SHALL have an accessible name that states what it filters.

#### Scenario: Toolbar filters

- **WHEN** the user opens gestiones, presupuestos or folios
- **THEN** the filter is announced as "Filtrar por cliente" or "Filtrar por estado"

#### Scenario: Conditional selects

- **WHEN** auditoria shows its module filter or estados-gestion its workflow selector
- **THEN** it is announced as "Módulo" or "Workflow a visualizar"

#### Scenario: No unnamed select

- **WHEN** src/app is scanned
- **THEN** every SelectTrigger and select has a name

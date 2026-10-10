# ui-accessibility — delta

## Purpose

Dashboard controls and dialogs expose accessible names in the active locale.

## MODIFIED Requirements

### Requirement: Dialogs are named by their title

Every dashboard dialog SHALL be labelled by its visible title through `aria-labelledby`, and its close button SHALL be named `common.close` in the active locale.

#### Scenario: Create dialogs

- **WHEN** the user opens a create dialog on personas, presupuestos, gestiones, escrituras or pagos
- **THEN** the dialog is announced with its title and the close button as "Cerrar"

#### Scenario: English

- **WHEN** the locale is en
- **THEN** the dialog is "New person" and the close button "Close"

#### Scenario: Every dialog

- **WHEN** src/app is scanned
- **THEN** each DialogContent has a dialogTitle heading or a DialogTitle

## ADDED Requirements

### Requirement: People dialog FormFields expose the shared contract

The people create/edit dialog SHALL compose with `FormContainer` → `FormSection` → `FormField` / `CheckboxField` → `FormActions`. Every text and select control SHALL have a visible label (not placeholder-only). Required fields SHALL show a visible required indicator and expose `aria-required`. When validation fails, error text SHALL use `role="alert"`, be linked via `aria-describedby`, and set `aria-invalid` on the control.

#### Scenario: Required fields announce and show errors

- **WHEN** a clerk opens New person and clicks Create with empty required fields
- **THEN** each empty required control is `aria-invalid` with an alert description and a visible required marker on its label

#### Scenario: Labels are not placeholder-only

- **WHEN** the people create dialog is open
- **THEN** first name, last name, identification type, and identification number each have an accessible name from a visible label

### Requirement: Loading, empty, and primary touch target

While create or update is pending, the primary save control SHALL be disabled (and preferably busy). The people list empty state SHALL name the gap and offer the create action. The dialog primary action SHALL be at least 24 CSS px tall (default/large button token prefers 44px / `h-11`).

#### Scenario: Save disables while pending

- **WHEN** create or update mutation is pending
- **THEN** the dialog primary save button is disabled

#### Scenario: Primary save meets touch floor

- **WHEN** the people create dialog is open at desktop or 320px width
- **THEN** the primary Create/Update button box height is at least 24 CSS pixels and uses the default (or larger) button size token

#### Scenario: Empty list offers create

- **WHEN** the people list has no rows
- **THEN** the empty state describes the gap and exposes a create action

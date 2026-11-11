# ui-admin-catalog-forms-a11y Specification

## Purpose
Locks rendered accessibility of administration catalog create/edit forms so clerks can complete Operate tasks with the shared FormField contract at desktop and 320px.

## Requirements

### Requirement: Catalog dialog FormFields expose the shared contract

Administration catalog create/edit dialogs SHALL compose with `FormContainer` → `FormSection` → `FormField` / `CheckboxField` → `FormActions`. Every text and select control SHALL have a visible label (not placeholder-only). Required fields SHALL show a visible required indicator and expose `aria-required`. When client validation fails, error text SHALL use `role="alert"`, be linked via `aria-describedby`, and set `aria-invalid` on the control. Shared contract drift SHALL be corrected in shared components when the gap is shared.

#### Scenario: Required fields announce and show errors

- **WHEN** a clerk opens a catalog create dialog with required fields and activates Save with those fields empty
- **THEN** each empty required control is `aria-invalid` with an alert description and a visible required marker on its label

#### Scenario: Labels are not placeholder-only

- **WHEN** a catalog create dialog is open
- **THEN** each text and select control has an accessible name from a visible label

#### Scenario: Shared checkbox controls meet the touch floor

- **WHEN** a catalog dialog uses checkbox controls from the shared contract
- **THEN** each checkbox hit target is at least 24 CSS pixels wide and tall

### Requirement: Loading, empty, primary touch, and narrow viewport

While create or update is pending, the primary save control SHALL be disabled and SHALL expose a busy state. A catalog list empty state SHALL name the gap. The dialog primary action SHALL be at least 24 CSS px tall (default/large button token prefers 44px / `h-11`) at desktop and at 320px, and the page SHALL not force horizontal overflow at 320px.

#### Scenario: Save disables while pending

- **WHEN** create or update mutation is pending on a catalog dialog
- **THEN** the dialog primary save button is disabled and marked busy

#### Scenario: Primary save meets touch floor at desktop and 320px

- **WHEN** a catalog create dialog is open at 1280px or 320px width
- **THEN** the primary Save/Create button box height is at least 24 CSS pixels and uses the default (or larger) button size token

#### Scenario: Narrow viewport does not clip the primary action

- **WHEN** a catalog create dialog is open at 320px width
- **THEN** the primary action remains visible and the document does not require horizontal scrolling

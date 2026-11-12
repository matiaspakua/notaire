# icon-button-label Specification

## Purpose

Icon-only actions expose a translated aria-label as their accessible name.

## Requirements

### Requirement: Icon-only row actions have an aria-label

Every icon-only action button in the payments list SHALL expose a translated `aria-label`; a `title` alone is not the accessible name.

#### Scenario: receipt action

- **WHEN** the payments list renders a row
- **THEN** the receipt button has an aria-label equal to its title text

#### Scenario: edit and delete keep their names

- **WHEN** the payments list renders a row
- **THEN** the edit and delete buttons are reachable by their accessible names

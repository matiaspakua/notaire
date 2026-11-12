<!-- Governed by CONSTITUTION.md. CU76, issue #2277. -->

## ADDED Requirements

### Requirement: Factory UI shells can shrink on narrow viewports

The backoffice and runtime HTML shells SHALL let their `main` element shrink (`min-width: 0`) so that wide content such as a JSON report scrolls inside its own container and the page has no horizontal scroll at 360 px.

#### Scenario: backoffice shell

- **WHEN** the backoffice HTML is rendered
- **THEN** its `main` rule contains `min-width: 0`

#### Scenario: runtime shell

- **WHEN** the runtime HTML is rendered
- **THEN** its `main` rule contains `min-width: 0`

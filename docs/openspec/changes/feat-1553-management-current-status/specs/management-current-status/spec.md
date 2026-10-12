# management-current-status — delta

## Purpose

Showing a management's current status in the UI.

## MODIFIED Requirements

### Requirement: Current status in the case summary

The case summary SHALL show the management's current status (name, date and notes when present) from GET /managements/{id}/current-status, SHALL show 'Sin estado registrado' on 404, and SHALL reflect a status transition.

#### Scenario: History-based

- **WHEN** a History row exists
- **THEN** status, date and notes are shown

#### Scenario: Synthesized

- **WHEN** no History row
- **THEN** the status is shown without notes

#### Scenario: No status

- **WHEN** the endpoint answers 404
- **THEN** 'Sin estado registrado'

#### Scenario: After transition

- **WHEN** a CU83 transition to the final status
- **THEN** the summary shows the new status

#### Scenario: Mobile

- **WHEN** 390px
- **THEN** no horizontal overflow

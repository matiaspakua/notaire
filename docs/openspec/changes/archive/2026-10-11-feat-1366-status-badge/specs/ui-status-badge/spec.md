# ui-status-badge — delta

## Purpose

Status columns render one shared, accessible status badge.

## ADDED Requirements

### Requirement: One status badge for every status column

The budgets, cases, testimonies, protocol, users and folios lists SHALL render their status with one shared badge that shows a tone, a decorative icon and a text label; fixed vocabularies SHALL be translated; a value outside a vocabulary SHALL be shown as stored, and a missing value as a dash.

#### Scenario: Every status column uses the badge

- **WHEN** a user opens any of the six lists
- **THEN** each row's status is a badge with a tone, an icon hidden from assistive technology and a text label

#### Scenario: Unknown value shown as stored

- **WHEN** a status value is not in its domain's vocabulary
- **THEN** the badge is neutral and shows the stored text

#### Scenario: Labels translated

- **WHEN** the active locale is English
- **THEN** fixed-vocabulary statuses show their English label

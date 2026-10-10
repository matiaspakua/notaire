# ui-dashboard — delta

## Purpose

Dashboard summary cards.

## MODIFIED Requirements

### Requirement: Dashboard stat cards

Each dashboard stat card SHALL show the exact API total for the locale, SHALL show a loading skeleton (not 0) until the total arrives and a dash if it fails, and the dashboard SHALL NOT request lists with size=1000.

#### Scenario: Loading

- **WHEN** a count has not arrived
- **THEN** the card shows a skeleton and is aria-busy

#### Scenario: Loaded

- **WHEN** the counts arrive
- **THEN** each card equals the API totalElements, formatted for es-AR

#### Scenario: No list downloads

- **WHEN** the dashboard loads
- **THEN** no request has size=1000

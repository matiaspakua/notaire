# dashboard-expirations-alert — delta

## Purpose

Alerting about upcoming document expirations on the home dashboard.

## MODIFIED Requirements

### Requirement: Upcoming-expirations alert

The home dashboard SHALL show the number of documents expiring within the upcoming-expirations default window, the three nearest, and a link to the full list, and SHALL render nothing when there are none or the request fails.

#### Scenario: With data

- **WHEN** four documents expiring in the window
- **THEN** the count is 4 and the 3 nearest are listed

#### Scenario: Empty

- **WHEN** no documents expiring
- **THEN** no card

#### Scenario: Error

- **WHEN** the request fails
- **THEN** no card; the dashboard renders

#### Scenario: Link

- **WHEN** the 'Ver todos' link
- **THEN** opens /dashboard/upcoming-expirations listing the seeded document

#### Scenario: Mobile

- **WHEN** 390px
- **THEN** the card fits without horizontal overflow

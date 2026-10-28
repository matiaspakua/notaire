## ADDED Requirements

### Requirement: Shared report download card

The reports page SHALL compose downloads with a shared `ReportCard` that uses
design-system tokens (sentence-case title, description, primary icon tint, full-width
secondary download action).

#### Scenario: Report card exposes download control

- **WHEN** a ReportCard is rendered with a download test id
- **THEN** the download control is present and disabled when `canDownload` is false

### Requirement: Shared home metric card

Dashboard home totals SHALL render via a shared `MetricCard` wrapping `StatValue`
with the apple-shadow metric tile language.

#### Scenario: Metric card shows label and value slot

- **WHEN** MetricCard is rendered with a label and children
- **THEN** the label and children are visible inside a metric tile

### Requirement: Reports catalog load states

When notary or folio-type catalogs fail to load, the reports page SHALL show
ErrorState with retry. While both are loading with no data, it MAY show LoadingState.

#### Scenario: Catalog error invites retry

- **WHEN** the notaries query is in error
- **THEN** an ErrorState with retry is shown

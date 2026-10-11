# frontend-i18n-page-coverage — delta

## Purpose

Screens are fully localised, including formatted dates, numbers and money.

## ADDED Requirements

### Requirement: Formatted values follow the active locale

Screens SHALL format dates, timestamps, numbers and money through one locale-aware formatter: `es` uses es-AR, `en` uses en-GB (day first), and money is always ARS. No screen SHALL hardcode a locale.

#### Scenario: English formats

- **WHEN** the UI language is English and a payment of 75123.45 on 2026-07-08 is listed
- **THEN** the row shows `08/07/2026` and `ARS 75,123.45`

#### Scenario: Spanish formats

- **WHEN** the UI language is Spanish and the same payment is listed
- **THEN** the row shows `8/7/2026` and `$ 75.123,45`

#### Scenario: No hardcoded locale

- **WHEN** the screen sources are scanned
- **THEN** none names es-AR, en-US or en-GB or calls the es-AR-only helpers

# payment-consult — delta

## Purpose

Consulting payments by budget and date.

## MODIFIED Requirements

### Requirement: Payments by date range

The payments page SHALL filter payments between two days, both inclusive, kept in the URL, and SHALL reject an inverted range in the UI and with HTTP 400 in the API.

#### Scenario: Inclusive

- **WHEN** payments on 10, 15 and 20 Feb and a 10–15 Feb range
- **THEN** the 10 and 15 Feb payments are listed

#### Scenario: With budget

- **WHEN** a budget and a range
- **THEN** only that budget's payments in the range, no `/payments/date` call

#### Scenario: Inverted UI

- **WHEN** from after to
- **THEN** an inline error, no request, no rows

#### Scenario: Inverted API

- **WHEN** startDate after endDate
- **THEN** 400

#### Scenario: Mobile

- **WHEN** 390px
- **THEN** inputs usable, cards listed

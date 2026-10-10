# ui-calendar-dates — delta

## Purpose

Date-only fields are shown, edited and defaulted as calendar days; timestamps are shown in the business time zone.

## MODIFIED Requirements

### Requirement: Calendar dates round-trip as the stored day

A date-only field SHALL display, pre-fill `<input type="date">` and save as the calendar day the backend stored, independently of the server and browser time zones; today defaults SHALL be the business-zone day.

#### Scenario: Display in any zone

- **WHEN** the payments list renders in Buenos Aires, Madrid or UTC
- **THEN** a payment stored on 2026-08-07 shows 7/8/2026

#### Scenario: Edit pre-fill and round trip

- **WHEN** a budget or payment dated 2026-09-05 is edited in Buenos Aires or Madrid
- **THEN** the date input shows 2026-09-05, and saving unchanged keeps it

#### Scenario: Helpers

- **WHEN** server-midnight instants from UTC-3 to UTC+2, yyyy-MM-dd and empty values are parsed
- **THEN** the stored day, "" or — is returned; today is the business-zone day

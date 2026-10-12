# substitution-consult — delta

## Purpose

Consulting substitutions by year and notary.

## MODIFIED Requirements

### Requirement: Substitutions by year and notary

The substitutions page SHALL filter by a year (date-range overlap) and by notary name on either side, accent- and case-insensitive, kept in the URL.

#### Scenario: Overlap

- **WHEN** a Dec 2019 → Jan 2020 substitution
- **THEN** it is listed for 2019 and for 2020

#### Scenario: Notary

- **WHEN** a search for 'perez'
- **THEN** substitutions where Pérez is either side match

#### Scenario: Empty

- **WHEN** a year with no substitutions
- **THEN** 'No hay suplencias para {year}'

#### Scenario: Detail

- **WHEN** a row's edit action
- **THEN** the dialog shows the dates

#### Scenario: Mobile

- **WHEN** 390px
- **THEN** filters usable, no overflow

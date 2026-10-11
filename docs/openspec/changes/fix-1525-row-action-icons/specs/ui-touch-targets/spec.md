# ui-touch-targets — delta

## Purpose

Row action buttons are usable: sized hit areas and visible icons.

## MODIFIED Requirements

### Requirement: Visible row action icons

An icon inside a row action button SHALL render at its declared size regardless of the column width; it SHALL NOT collapse to 0px.

#### Scenario: Desktop table

- **WHEN** an admin opens Management Statuses at 1440px
- **THEN** the edit and delete icons in each row are 16x16

#### Scenario: Mobile cards

- **WHEN** an admin opens Concepts at 390px
- **THEN** the card action icons are 16x16

#### Scenario: Guard

- **WHEN** someone removes shrink-0 or max-w-none from NotaireIcon
- **THEN** the unit suite fails

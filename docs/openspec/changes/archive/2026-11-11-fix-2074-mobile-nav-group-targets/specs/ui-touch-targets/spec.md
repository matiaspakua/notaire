# ui-touch-targets — delta (#2074)

## Purpose

Extend touch-target coverage to `AppSidebar` mobile-sheet group disclosures.

## ADDED Requirements

### Requirement: Mobile nav group toggles

In the mobile navigation sheet (viewport below 768px), every nav-group disclosure
button SHALL be at least 44 CSS pixels tall. In the desktop sidebar column, group
disclosure buttons MAY keep a compact 32px height.

#### Scenario: Mobile sheet

- **WHEN** the mobile navigation sheet is open
- **THEN** each `nav-group-*` disclosure button is at least 44px tall (`min-h-11`)

#### Scenario: Desktop sidebar

- **WHEN** the desktop sidebar is shown at ≥768px
- **THEN** each group disclosure button keeps the compact `min-h-8` height

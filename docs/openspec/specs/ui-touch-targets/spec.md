# ui-touch-targets Specification

## Purpose
Interactive controls meet the 44x44 minimum on touch screens.
## Requirements
### Requirement: Touch targets

On devices whose primary pointer is coarse, every small button and the rows-per-page select SHALL be at least 44x44 CSS pixels, adjacent row actions SHALL be 8px apart, and with a fine pointer small buttons SHALL keep their 32px height.

#### Scenario: Phone

- **WHEN** a user opens a list page on a 390px touch phone
- **THEN** every visible button and select in the page content is at least 44x44

#### Scenario: Tablet

- **WHEN** a user opens a list page on an 820px touch tablet
- **THEN** every visible button and select in the page content is at least 44x44

#### Scenario: Desktop

- **WHEN** a user opens /dashboard/people with a mouse at 1440px
- **THEN** the row actions stay 32px tall

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


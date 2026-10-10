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

- **WHEN** a user opens /dashboard/personas with a mouse at 1440px
- **THEN** the row actions stay 32px tall


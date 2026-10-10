# ui-navigation — delta

## Purpose

Mobile navigation drawer behaviour.

## MODIFIED Requirements

### Requirement: The mobile drawer is a modal navigation sheet

Below 768px the navigation drawer SHALL move focus inside on open, trap Tab, close on Escape or its close button returning focus to the toggle, lock page scroll, and its toggle SHALL expose aria-expanded and aria-controls.

#### Scenario: Keyboard

- **WHEN** the drawer is open at 390px
- **THEN** focus is on the first link, Tab stays inside, Escape closes it and focus returns to the toggle

#### Scenario: Close button

- **WHEN** the drawer is open
- **THEN** the translated close button closes it

## ADDED Requirements

### Requirement: Collapsible sidebar

The dashboard sidebar SHALL offer a toggle that collapses it to an icon rail and expands it again.
The toggle SHALL carry `aria-expanded` and a translated label. Collapsed, every navigation item SHALL
keep an accessible name and the current page SHALL stay marked. The choice SHALL persist across
reloads in the same browser; a storage failure only loses the preference. At widths of 768px or less
the toggle SHALL be hidden and the layout unchanged.

#### Scenario: collapse and expand

- **GIVEN** a signed-in user on the overview at 1280px
- **WHEN** the user activates the collapse toggle
- **THEN** the sidebar narrows to icons, the toggle reports `aria-expanded="false"`, and each nav link keeps its name

#### Scenario: the choice persists

- **GIVEN** the sidebar is collapsed
- **WHEN** the page reloads
- **THEN** the sidebar is still collapsed

#### Scenario: narrow screens are unchanged

- **GIVEN** a viewport of 375px
- **WHEN** the page renders
- **THEN** the collapse toggle is not visible

# pages-platform-contrast Specification

## Purpose
TBD - created by archiving change fix-2014-pages-platform-contrast. Update Purpose after archive.
## Requirements
### Requirement: Platform Observability layer meets contrast

The Observability architecture card on `/product/platform/` SHALL meet WCAG 2 AA color-contrast for its label and tech badges under axe.

#### Scenario: Platform page axe clean

- **GIVEN** the pages Playwright suite
- **WHEN** `/product/platform/` is checked on desktop and mobile
- **THEN** axe reports no color-contrast violations on the Observability card


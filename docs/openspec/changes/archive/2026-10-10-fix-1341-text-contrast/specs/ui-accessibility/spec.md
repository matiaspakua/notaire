# ui-accessibility — delta

## Purpose

Dashboard text and controls meet WCAG 2.1 AA.

## MODIFIED Requirements

### Requirement: Text meets AA contrast

Every visible text element on dashboard routes and /login SHALL have a contrast ratio of at least 4.5:1 with its background, or 3:1 for large text.

#### Scenario: Light backgrounds

- **WHEN** a text token is painted on neutral[0], [50], [100] or the table header gray
- **THEN** its ratio is at least 4.5:1

#### Scenario: Pages

- **WHEN** the user opens /login or a dashboard route at 1440px or 390px
- **THEN** no visible text is below 4.5:1 (3:1 large)

#### Scenario: No light-gray text utilities

- **WHEN** src/app and src/components are scanned
- **THEN** no text-{neutral,gray,slate,zinc}-{300,400} and no neutral-500 on neutral-100/200

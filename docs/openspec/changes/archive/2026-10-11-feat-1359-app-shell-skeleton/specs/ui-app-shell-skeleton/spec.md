# ui-app-shell-skeleton — delta

## Purpose

The dashboard paints a static app shell before it mounts, and loading placeholders respect reduced motion.

## ADDED Requirements

### Requirement: No blank first paint

Every dashboard route SHALL render a static app-shell skeleton (sidebar frame, header row, content blocks) until the client has mounted and the session is known, SHALL replace it with the real shell without layout shift, and its loading placeholders SHALL NOT pulse when the user prefers reduced motion.

#### Scenario: First paint is the app shell

- **WHEN** a user hard-loads a dashboard URL and no script has run yet
- **THEN** the page shows a busy, labelled app-shell skeleton instead of a blank page

#### Scenario: Real shell replaces it without layout shift

- **WHEN** the client mounts with a session
- **THEN** the real sidebar and page replace the skeleton and cumulative layout shift stays under 0.05

#### Scenario: No pulsing under reduced motion

- **WHEN** the user prefers reduced motion
- **THEN** skeleton blocks are a static tint

#### Scenario: Loading lists show skeleton rows

- **WHEN** a list is loading
- **THEN** the table is busy and shows skeleton rows

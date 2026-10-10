# keyboard-navigation — delta

## Purpose

Keyboard and screen-reader navigation of the dashboard shell.

## ADDED Requirements

### Requirement: Skip to content

Every dashboard page SHALL start with a "skip to content" link, as its first focusable element. The link SHALL be visible while focused and SHALL move focus to the main content landmark.

#### Scenario: First Tab on a dashboard page

- **WHEN** the user presses Tab once on `/dashboard/personas`
- **THEN** the visible "Saltar al contenido" link is focused

#### Scenario: Activating the skip link

- **WHEN** the user presses Enter on the skip link
- **THEN** focus is on `<main id="main-content">` and the next Tab stays inside the page content

### Requirement: Focus on route change

After a client-side navigation to another pathname, focus SHALL move to the new page's `<h1>`. The first load and query-only URL changes SHALL NOT move focus.

#### Scenario: Clicking a sidebar link

- **WHEN** the user clicks "Gestiones" in the sidebar while on Personas
- **THEN** the focused element is the Gestiones page `<h1>`

#### Scenario: First load

- **WHEN** a dashboard page loads
- **THEN** focus stays on the document

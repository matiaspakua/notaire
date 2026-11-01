## ADDED Requirements

### Requirement: Diagrams are driven by the reader

Every interactive diagram SHALL start on its first step and change only when the reader presses Previous, Play,
Next, an arrow key, a step in the list, or a part of the drawing. Play SHALL go through the steps once and stop.

#### Scenario: Reader opens a page with a diagram

- **WHEN** a reader opens `/docs/hexagonal/` and waits
- **THEN** the request-flow diagram stays on step 1

#### Scenario: Reader steps with the keyboard

- **WHEN** focus is on the diagram controls and the reader presses the right arrow
- **THEN** the next step is shown and announced through a polite live region

### Requirement: Diagrams work without motion and without the drawing

With `prefers-reduced-motion: reduce` every change SHALL be instant. Every step SHALL be readable from the step
list, which is shown at every width, and the drawing SHALL have an accessible name and description.

#### Scenario: Reader on a 375 px phone

- **WHEN** the page is 375 px wide
- **THEN** each diagram shows its controls, current step and step list, with no horizontal scroll and no axe violation

### Requirement: Diagram counts come from the repository

Counts and names in the diagrams SHALL come from `lib/tech-facts.ts` or name real files in the repository.

#### Scenario: A workflow is added

- **WHEN** a new workflow with a `pull_request` trigger is committed and the site is rebuilt
- **THEN** the CI/CD diagram lists it under Pull request without an edit

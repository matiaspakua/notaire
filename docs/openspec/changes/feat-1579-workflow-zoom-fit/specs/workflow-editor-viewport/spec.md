# workflow-editor-viewport — delta

## Purpose

Zoom, pan, fit-to-view and the MiniMap of the workflow graph.

## MODIFIED Requirements

### Requirement: Zoom, pan and fit-to-view

The workflow graph SHALL bound zoom between 0.1 and 2, SHALL name its controls in the user's language, SHALL show every box after fit-to-view on desktop and phone, and SHALL show a MiniMap colored by type on screens md and up.

#### Scenario: Fit

- **WHEN** zoomed in
- **THEN** fit-to-view brings every box inside the canvas

#### Scenario: Phone fit

- **WHEN** 390px viewport
- **THEN** fit-to-view shows every box

#### Scenario: Labels

- **WHEN** Spanish UI
- **THEN** controls named Acercar, Alejar, Ajustar a la vista

#### Scenario: MiniMap

- **WHEN** desktop
- **THEN** one MiniMap node per box, colored by type

#### Scenario: MiniMap phone

- **WHEN** 390px viewport
- **THEN** MiniMap hidden

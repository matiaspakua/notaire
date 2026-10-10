# ui-personas — delta

## Purpose

Personas list search.

## MODIFIED Requirements

### Requirement: Personas search

The personas search SHALL send `GET /people/search` about 300ms after the user stops typing, SHALL key its cache by primitive criteria only, and SHALL keep the previous results on screen while the next ones load.

#### Scenario: Typing a surname

- **WHEN** the user types a surname quickly
- **THEN** one `/people/search` request is sent, with the full surname

#### Scenario: Refining a search

- **WHEN** the user changes the criteria after results are shown
- **THEN** the previous results stay visible until the new ones arrive

#### Scenario: Clearing the search

- **WHEN** every criterion is cleared
- **THEN** the table shows the server page and no search request is sent

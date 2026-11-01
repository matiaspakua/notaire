## ADDED Requirements

### Requirement: The Technical docs explain the architecture internals

The Technical docs SHALL include a Technology stack page and an "Architecture internals" group with pages for the
hexagonal architecture, the database and Flyway, and the API contract and Bruno suite.

#### Scenario: Reader looks for how the database is versioned

- **WHEN** a reader searches the site for "flyway"
- **THEN** the Database & Flyway page is the first result

### Requirement: Versions and counts are read from the repository

Every version, migration, endpoint, request, port, workflow and ADR shown on those pages SHALL be read from the
repository at build time; a value that cannot be read SHALL be shown as missing, not guessed.

#### Scenario: A migration is added

- **WHEN** a new `V{n}__*.sql` migration is committed and the site is rebuilt
- **THEN** the Database page lists it and its count without an edit

### Requirement: Cited repository paths exist

Every repository path the new pages cite SHALL exist on the branch.

#### Scenario: A cited file is renamed

- **WHEN** a file cited by a Technical docs page is moved
- **THEN** `workspace/tests/test_pages_tech_docs.py` fails

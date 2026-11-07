# github-pages-technical-docs Specification

## Purpose

TBD - created by archiving change docs-1976-product-sidebar-tech-docs. Update Purpose after archive.

## Requirements

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

### Requirement: The Technical docs tell the engineering artifact chain

The Technical docs SHALL include an Engineering artifacts page that walks the artifact chain in order: analyze, define, design, specify, build, test, secure, release and operate. For each artifact it SHALL say what it is, why it exists, the method and tool behind it, and the repository path where it lives. Every count it shows SHALL be read from the repository at build time and SHALL show as missing when it cannot be read. Every path it cites SHALL exist.

#### Scenario: A reader looks for the threat model

- **WHEN** a reader searches the site for "threat model"
- **THEN** the Engineering artifacts page is among the results and its security stage links `THREAT-MODEL-SYSTEM.md`

#### Scenario: A use case is added

- **WHEN** a new `CUxx` file is committed and the site is rebuilt
- **THEN** the use-case count on the Engineering artifacts page changes without an edit to the page

### Requirement: Explainer figures are honest and accessible

The artifact cabinet and the gated line figures SHALL draw their numbers from the same data the page prints. They SHALL be operable with the keyboard, SHALL stay still under reduced motion, and SHALL read at 390 px wide.

#### Scenario: Reduced motion

- **WHEN** the reader's system asks for reduced motion
- **THEN** neither figure animates and both show their resting state with a correct readout

#### Scenario: Keyboard

- **WHEN** a keyboard user focuses the cabinet and presses the arrow keys
- **THEN** the next drawer opens and the readout names its artifact and count

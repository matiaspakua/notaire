# retire-deprecated-frontend-swing Specification

## Purpose

Remove the dead Swing client tree that still declares EOL Log4j 1.x so
Dependabot critical/high alerts from that path disappear. Source: #1046; CU78.
Related open cleanup issue #585 covers `src.old` / `deprecated-src.old`, not
this deletion.

## Requirements

### Requirement: deprecated-frontend-swing tree is removed from main

The repository on `main` MUST NOT contain a `deprecated-frontend-swing/`
directory (nor a resurrected active `frontend-swing/` Maven module). Deletion
is the remediation for `log4j:log4j:1.2.17` alerts; the dead client MUST NOT be
“fixed” in place.

#### Scenario: deprecated-frontend-swing directory is absent

- **WHEN** the repository tree at the change’s merge commit is listed
- **THEN** no `deprecated-frontend-swing/` path exists

#### Scenario: no active frontend-swing module returns

- **WHEN** the root `pom.xml` modules and top-level directories are inspected
- **THEN** there is no `frontend-swing` module and no top-level `frontend-swing/`
  directory

### Requirement: Log4j 1.x alerts from Swing are eliminated

Tracked Maven POM files in the active product tree MUST NOT declare
`log4j:log4j` (Log4j 1.x). Removing the Swing POM is sufficient to satisfy this
requirement for the alerts named in #1046.

#### Scenario: no log4j:log4j dependency remains in tracked POMs

- **WHEN** all tracked `pom.xml` files are searched for `log4j:log4j` /
  `groupId>log4j</groupId>` with artifact `log4j`
- **THEN** no such dependency declaration remains

### Requirement: Live references stop implying Swing is present

Live (non-archive) ownership and top-level docs MUST NOT point at a present
Swing module path after deletion.

#### Scenario: CODEOWNERS has no live frontend-swing path

- **WHEN** `.github/CODEOWNERS` is inspected
- **THEN** it does not contain a `/frontend-swing/` (or
  `/deprecated-frontend-swing/`) ownership path for a directory that no longer
  exists

#### Scenario: root README does not list deprecated-frontend-swing as present

- **WHEN** the root `README.md` project tree / module list is inspected
- **THEN** it does not present `deprecated-frontend-swing/` as an on-disk
  module (it MAY mention historical removal in prose)

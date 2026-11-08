# catalogs Specification

## Purpose
Catalogs CRUD for skills, MCP, agents, and memory as digested JSON entries. Update Purpose after archive.

## Requirements

### Requirement: Catalog CRUD
The kit SHALL provide CLI CRUD for skills, mcp, agents, and memory catalog entries as JSON files with digests.

#### Scenario: add list remove
- **WHEN** an entry is added then listed then removed
- **THEN** list reflects each state and files match

### Requirement: List and validate never execute
Catalog list and validate SHALL only read files and MUST NOT execute entry payloads.

#### Scenario: hostile summary
- **WHEN** an entry summary contains a shell command string
- **THEN** validate still passes without running it

### Requirement: Unknown kinds fail closed
Unknown catalog kinds SHALL exit with a configuration error.

#### Scenario: widgets
- **WHEN** catalog list --kind widgets
- **THEN** exit code is 2

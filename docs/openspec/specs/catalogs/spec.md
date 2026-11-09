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

### Requirement: Skills discoverability via catalog list
When `[skills] dir` is set, `aisdlc catalog list` SHALL include folders with `SKILL.md` as read-only entries with `source=skills-dir` that are not already present as JSON catalog entries. `catalog validate` SHALL skip virtual discover rows.

#### Scenario: Empty JSON catalog still lists skills-dir entries
- **WHEN** catalogs JSON is empty and skills dir has SKILL.md folders
- **THEN** catalog list --kind skills returns those ids with source skills-dir

### Requirement: Stage profile refs use catalog resolution
Stage profile agent/skill/mcp ids SHALL resolve using the same catalog listing
surface as `catalog list` (JSON entries under `[catalogs] dir`, plus skills-dir
overlay for skills). Resolution is read-only and MUST NOT execute entry payloads.

#### Scenario: stage skill resolves via skills-dir overlay
- **WHEN** a stage skill id matches a skills-dir discover row and catalogs JSON
  has no matching skills entry
- **THEN** stage validation accepts the skill id

### Requirement: Context budget refs use memory catalog resolution
Profile `[context].memory` pack ids SHALL resolve using the same catalog listing
surface as `catalog list --kind memory` (JSON entries under `[catalogs] dir`).
Resolution is read-only and MUST NOT execute entry payloads.

#### Scenario: context memory resolves via catalog entry
- **WHEN** `[context].memory` names an id present as a memory catalog JSON entry
- **THEN** context validation accepts the pack id

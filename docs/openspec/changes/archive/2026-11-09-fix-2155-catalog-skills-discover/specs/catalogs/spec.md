## ADDED Requirements

### Requirement: Skills discoverability via catalog list
When `[skills] dir` is set, `aisdlc catalog list` SHALL include folders with `SKILL.md` as read-only entries with `source=skills-dir` that are not already present as JSON catalog entries. `catalog validate` SHALL skip virtual discover rows.

#### Scenario: Empty JSON catalog still lists skills-dir entries
- **WHEN** catalogs JSON is empty and skills dir has SKILL.md folders
- **THEN** catalog list --kind skills returns those ids with source skills-dir

## ADDED Requirements

### Requirement: Stage profile refs use catalog resolution
Stage profile agent/skill/mcp ids SHALL resolve using the same catalog listing
surface as `catalog list` (JSON entries under `[catalogs] dir`, plus skills-dir
overlay for skills). Resolution is read-only and MUST NOT execute entry payloads.

#### Scenario: stage skill resolves via skills-dir overlay
- **WHEN** a stage skill id matches a skills-dir discover row and catalogs JSON
  has no matching skills entry
- **THEN** stage validation accepts the skill id

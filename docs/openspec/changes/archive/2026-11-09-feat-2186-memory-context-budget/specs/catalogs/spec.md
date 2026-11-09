## ADDED Requirements

### Requirement: Context budget refs use memory catalog resolution
Profile `[context].memory` pack ids SHALL resolve using the same catalog listing
surface as `catalog list --kind memory` (JSON entries under `[catalogs] dir`).
Resolution is read-only and MUST NOT execute entry payloads.

#### Scenario: context memory resolves via catalog entry
- **WHEN** `[context].memory` names an id present as a memory catalog JSON entry
- **THEN** context validation accepts the pack id

## ADDED Requirements

### Requirement: Roadmap mirrors factory spine status
The in-repo FACTORY-ROADMAP SHALL state that epic #2134's P0–P3 Active spine is
complete, retain the landed issue numbers for traceability, note validation
follow-ons (#2153/#2155/#2157) and preflight evidence (#2159), and SHALL NOT tell
later sessions to pick an already-closed P0 spine issue as next work.

#### Scenario: Roadmap states spine done
- **WHEN** an agent reads `FACTORY-ROADMAP.md` after ASLI-31
- **THEN** the phased backlog marks the #2134 Active spine complete (not as open
  ordered work) and points continue-sessions at deferred/follow-on work only

#### Scenario: Issue needles retained
- **WHEN** `FactoryRoadmapTest` scans the roadmap
- **THEN** issue references #2125–#2142 and #2134 remain present for the mirror
  guard

## ADDED Requirements

### Requirement: Migration inventory records extra harness stubs

The migration inventory MUST include discoverable harness-adapter rows for the
Claude Code, Codex, and ACP plugin stub directories under `framework/ai-sdlc/plugins/`,
and MUST still contain at least one planned row so it cannot claim the move is finished.

#### Scenario: inventory names the three stubs

- **WHEN** inventory check runs on the Notaire profile
- **THEN** it exits 0 and the three stub plugin paths are present as harness-adapter rows

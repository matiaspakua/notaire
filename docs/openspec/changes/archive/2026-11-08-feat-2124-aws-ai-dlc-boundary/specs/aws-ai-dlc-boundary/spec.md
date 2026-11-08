## ADDED Requirements

### Requirement: Design states which AI-DLC phases the factory allows

`plugins/aws-ai-dlc/DESIGN.md` MUST name AI-DLC phases Initialization through Operation and MUST state that AI-DLC owns zero aisdlc stages as orchestrator, with Construction and Operation forbidden as parallel runners.

#### Scenario: design note

- **WHEN** a reviewer opens DESIGN.md
- **THEN** all five AI-DLC phases and the owns-zero rule are present

### Requirement: Hooks are never auto-trusted

The aws-ai-dlc manifest MUST set auto_trust_hooks = false. config validate MUST fail when any plugin manifest sets auto_trust_hooks = true.

#### Scenario: reject auto-trust

- **WHEN** a manifest sets auto_trust_hooks = true
- **THEN** config validate exits 1 mentioning auto_trust_hooks

### Requirement: Plugin stays unavailable until later wiring

On Notaire, aws-ai-dlc MUST appear on plugin list as unavailable (trusted false / out of allow) with status boundary, and plugin plan MUST exit 2.

#### Scenario: Notaire profile

- **WHEN** plugin list and plan run
- **THEN** available is false and plan exits 2 while config validate exits 0

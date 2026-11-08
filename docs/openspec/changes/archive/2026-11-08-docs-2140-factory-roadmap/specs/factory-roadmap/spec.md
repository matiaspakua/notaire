## ADDED Requirements

### Requirement: Durable factory roadmap

The repository SHALL publish a permanent roadmap under `docs/300-development/306-ai-sdlc-framework/FACTORY-ROADMAP.md` that later sessions can continue from without the store assessments.

#### Scenario: roadmap covers the full factory goal

- **GIVEN** the permanent docs tree
- **WHEN** an agent opens `FACTORY-ROADMAP.md`
- **THEN** it names config-driven stages/agents/skills/MCP/models, Foreman, lead-time telemetry, OpenTelemetry, OpenFeature, OpenAPI, CLI+UI, install lifecycle, catalogs, plugins, self-improvement, local+cloud harnesses, Notaire validation, and TDD/KISS/DRY/SRP/boy scout

#### Scenario: foundation points at the roadmap

- **GIVEN** `FACTORY-FOUNDATION.md`
- **WHEN** a reader finishes the landed slice notes
- **THEN** they are pointed at `FACTORY-ROADMAP.md` for the multi-session plan

#### Scenario: TelemetryPort code stays out of this change

- **GIVEN** issue #2140 and parallel #2125
- **WHEN** this change merges
- **THEN** no OpenTelemetry SDK is added by #2140; #2125 remains the telemetry implementation Issue
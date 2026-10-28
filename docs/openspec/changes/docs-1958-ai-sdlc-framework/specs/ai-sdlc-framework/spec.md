<!-- Governed by CONSTITUTION.md. CU76, issue #1958. -->

## Purpose

Provide a stack-agnostic AI SDLC framework kit derived from Notaire evidence, with Notaire as the first adopter profile, plus a published case study.

## ADDED Requirements

### Requirement: Stack-agnostic kit with Notaire profile

The repository SHALL ship `framework/ai-sdlc/` with a profile schema, gate runner, templates, adoption guide, and `profiles/notaire` adopter. Kit code outside that profile MUST NOT hardcode Notaire product paths.

#### Scenario: Kit loads the Notaire profile

- **WHEN** the gate runner is invoked with `--profile notaire`
- **THEN** it resolves profile config from `framework/ai-sdlc/profiles/notaire` and reports configured gates without importing Notaire app source

#### Scenario: Kit refuses unknown profiles

- **WHEN** the gate runner is invoked with an unknown profile name
- **THEN** it exits non-zero with an actionable error

### Requirement: Evidence report

`ai_sdlc_framework.md` SHALL document purpose, principles, non-goals, components, workflow, roles, gates, state, observability, cost, adaptation, and derivation (evidence vs inference vs open questions) with at least one Mermaid diagram.

#### Scenario: Report exists with required sections

- **WHEN** the report file is read
- **THEN** it contains the mandatory section headings listed in the issue acceptance criteria

### Requirement: Case study on GitHub Pages

The static site SHALL expose an `/ai-sdlc/case-study` page with problem→solution narrative, workflow, agents/skills, examples, before/after, lessons and metrics, linked from the AI Factory nav.

#### Scenario: Case-study route is in the factory nav

- **WHEN** a reader opens the AI Factory section
- **THEN** the nav includes a Case study link to `/ai-sdlc/case-study/`

### Requirement: ui-design skill committed

`.claude/skills/ui-design` SHALL be committed and listed in `.claude/skills/README.md`.

#### Scenario: Skills index lists ui-design

- **WHEN** `.claude/skills/README.md` is read
- **THEN** it references the `ui-design` skill

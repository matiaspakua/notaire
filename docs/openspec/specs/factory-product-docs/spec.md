# factory-product-docs Specification

## Purpose

TBD - created by archiving change docs-2251-factory-product-analysis. Update Purpose after archive.

## Requirements

### Requirement: Factory design record is permanent and linked

The repository SHALL hold `FACTORY-PRODUCT-ANALYSIS.md` and `FACTORY-UI-SPEC.md` under `docs/300-development/306-ai-sdlc-framework/`, linked from `FACTORY-ROADMAP.md` and `framework/ai-sdlc/README.md`, and the UI spec SHALL end with the three Owner decisions unanswered.

#### Scenario: Design docs are linked from roadmap and kit README

- **WHEN** an agent opens `FACTORY-ROADMAP.md` or the kit README
- **THEN** both design docs are linked and exist

#### Scenario: Owner decisions stay open

- **WHEN** an agent reads `FACTORY-UI-SPEC.md`
- **THEN** it finds three decision packets and the marker that they are unanswered

<!-- Governed by CONSTITUTION.md. CU76, issue #2318. -->

## Purpose

Polish the RepoLens GitHub Pages showcase for craft and motion without changing metric honesty.

## ADDED Requirements

### Requirement: RepoLens page explains ranking with an interactive pipeline

The `/ai-sdlc/repolens/` page SHALL include a stepped diagram that walks parse → store → embed → rank → serve, using the site's FlowDiagram pattern, with reduced-motion support.

#### Scenario: Reader steps the ranking pipeline

- **WHEN** a visitor opens `/ai-sdlc/repolens/` and advances the pipeline controls
- **THEN** successive nodes and edges highlight to show how a query becomes ranked hits

#### Scenario: Reduced motion

- **WHEN** the visitor prefers reduced motion
- **THEN** the pipeline and chart entrances render without continuous or large animated transitions

### Requirement: Charts stay honest and become clearer

Benchmark charts SHALL keep values from the committed RepoLens bench snapshot, SHALL expose per-bar values on hover or keyboard focus, and SHALL remain readable in light and dark themes.

#### Scenario: Chart tooltip

- **WHEN** a visitor focuses or hovers a bar
- **THEN** the formatted value for that arm is announced or shown without changing the underlying number

#### Scenario: Snapshot still gates honesty

- **WHEN** unit tests run
- **THEN** headline factors still match the committed JSON via the existing snapshot loader

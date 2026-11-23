# frontend-dependency-guard Specification

## Purpose

TBD - created by archiving change fix-2440-xyflow-pin-12. Update Purpose after archive.

## Requirements

### Requirement: The workflow editor's graph library API is guarded

The frontend SHALL depend on an `@xyflow/react` major that provides `applyNodeChanges`, and a unit test
SHALL fail when the installed version does not. Dependabot SHALL NOT propose `@xyflow/react` majors
until the editor is migrated.

#### Scenario: an incompatible major is installed

- **GIVEN** `@xyflow/react` 13, which has no `applyNodeChanges`
- **WHEN** the frontend unit tests run
- **THEN** the xyflow API guard fails before the production build does

#### Scenario: the pinned version builds

- **GIVEN** `@xyflow/react` pinned to `^12.12.1`
- **WHEN** the frontend and the Pages demo are built
- **THEN** both builds succeed

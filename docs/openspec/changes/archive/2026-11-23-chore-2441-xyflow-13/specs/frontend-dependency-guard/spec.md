## MODIFIED Requirements

### Requirement: The workflow editor's graph library API is guarded

The frontend SHALL depend on `@xyflow/react` 13 and apply node changes through `NodeChangeset.applyTo`. A
unit test SHALL fail when the installed version does not provide that API. Dependabot SHALL propose
`@xyflow/react` majors again.

#### Scenario: an incompatible major is installed

- **GIVEN** an `@xyflow/react` version without `NodeChangeset.applyTo`
- **WHEN** the frontend unit tests run
- **THEN** the xyflow API guard fails before the production build does

#### Scenario: the pinned version builds

- **GIVEN** `@xyflow/react` at `^13.0.2`
- **WHEN** the frontend and the Pages demo are built
- **THEN** both builds succeed

#### Scenario: dragging a node keeps its position

- **GIVEN** the workflow editor on `@xyflow/react` 13
- **WHEN** a node is dragged
- **THEN** its new position is kept, as with version 12
